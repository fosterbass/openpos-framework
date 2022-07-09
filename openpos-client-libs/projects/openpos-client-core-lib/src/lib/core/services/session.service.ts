import { VERSIONS } from './../../version';
import { ILoading } from './../interfaces/loading.interface';

import { CONFIGURATION } from './../../configuration/configuration';
import { IMessageHandler } from './../interfaces/message-handler.interface';
import { PersonalizationService } from '../personalization/personalization.service';

import { Observable, Subscription, BehaviorSubject, Subject, merge, timer, ConnectableObservable, interval } from 'rxjs';
import { map, filter, takeWhile, publishReplay, take, debounce } from 'rxjs/operators';
import { Message } from '@stomp/stompjs';
import { Injectable, NgZone, Inject, ApplicationRef } from '@angular/core';
import { StompState, StompRService } from '@stomp/ng2-stompjs';
import { MatDialog } from '@angular/material/dialog';
// Importing the ../components barrel causes a circular reference since dynamic-screen references back to here,
// so we will import those files directly
import { LoaderState } from '../../shared/components/loader/loader-state';
import { IDeviceResponse } from '../oldplugins/device-response.interface';
import { HttpClient } from '@angular/common/http';
import { PingParams } from '../interfaces/ping-params.interface';
import { PingResult } from '../interfaces/ping-result.interface';
import { OpenposMessage } from '../messages/message';
import { MessageTypes } from '../messages/message-types';
import { ActionMessage } from '../messages/action-message';
import { CLIENTCONTEXT, IClientContext } from '../client-context/client-context-provider.interface';
import { DiscoveryService } from '../discovery/discovery.service';
import { UnlockScreenMessage } from '../messages/unlock-screen-message';
import { SplashScreen } from '../messages/splash-screen-message';
import { PowerStatus } from '../platform-plugins/power/power-supplier';

declare var window: any;
export class QueueLoadingMessage implements ILoading {
    type = 'Loading';
    title: string;
    queue = true;
    cancel = false;

    constructor(text: string) {
        this.title = text;
    }
}

export class ImmediateLoadingMessage implements ILoading {
    type = 'Loading';
    title: string;
    queue = false;
    cancel = false;

    constructor(text: string) {
        this.title = text;
    }
}

export class CancelLoadingMessage implements ILoading {
    type = 'Loading';
    cancel = true;
    queue = false;
}

export class ConnectedMessage {
    type = MessageTypes.CONNECTED;
}

export class DisconnectedMessage {
    type = MessageTypes.DISCONNECTED;
}

@Injectable({
    providedIn: 'root',
})
// Works around problem with re-establishing a STOMP connection
// as outlined here: https://github.com/stomp-js/ng2-stompjs/issues/58
export class OpenposStompService extends StompRService {
    disconnect() {
        if (this.client) {
            this.client.reconnect_delay = 0;
        }
        super.disconnect();
    }
}

@Injectable({
    providedIn: 'root',
})
export class SessionService implements IMessageHandler<any> {

    public state: Observable<string>;

    private subscription: Subscription;

    private authToken: string;

    private stompDebug = false;
    private stompUrl: string;

    public inBackground = false;

    private stompStateSubscription: Subscription;

    public onServerConnect: BehaviorSubject<boolean>;

    private stompJsonMessages$ = new BehaviorSubject<any>(false);

    private sessionMessages$ = new Subject<any>();

    private disconnectedMessage = LoaderState.DISCONNECTED_TITLE;

    private queryParams = new Map();

    private reconnecting = false;

    private reconnectTimerSub: Subscription;
    private connectedOnce = false;

    public screenMessage$: Observable<any>;

    public dialogMessage$: Observable<any>;

    private powerStatus: PowerStatus;

    constructor(
        public dialogService: MatDialog,
        public zone: NgZone,
        protected stompService: OpenposStompService,
        protected personalization: PersonalizationService,
        protected discovery: DiscoveryService,
        private http: HttpClient,
        @Inject(CLIENTCONTEXT) private clientContexts: Array<IClientContext>,
        private appRef: ApplicationRef
    ) {
        this.zone.onError.subscribe((e) => {
            if (typeof (e) === 'string') {
                console.error(`[OpenPOS] ${e}`);
            } else if (e.message) {
                console.error(`[OpenPOS] ${e.message}`, e);
            } else {
                console.error(`[OpenPOS] unexpected zone error`, e);
            }
        });
        this.onServerConnect = new BehaviorSubject<boolean>(false);

        this.registerMessageHandler(this);

        const screenMessagesBehavior = this.stompJsonMessages$.pipe(
            filter(message => message.type === MessageTypes.SCREEN && message.screenType !== 'NoOp'),
            publishReplay(1)
        ) as ConnectableObservable<any>;

        this.screenMessage$ = screenMessagesBehavior;

        const dialogMessagesBehavior = this.stompJsonMessages$.pipe(
            filter(message => message.type === MessageTypes.DIALOG && message.screenType !== 'NoOp'),
            publishReplay(1)
        ) as ConnectableObservable<any>;

        this.dialogMessage$ = dialogMessagesBehavior;

        // We need to capture incoming screen messages even with no subscribers, so make this hot 🔥
        screenMessagesBehavior.connect();
        dialogMessagesBehavior.connect();

        this.stompJsonMessages$.pipe(

            // If we receive many openpos messages in quick succession, wait for a
            // large enough open gap to run change detection in.
            debounce(() => interval(200))
        ).subscribe(() => {

            // There are some issues on some platforms (*cough* iOS *cough*) related
            // to the change detection not triggering on some changes. This is just
            // a little bit of help to let angular know, something has probably
            // changed and we need to run a detection cycle.

            try {
                console.debug('running application tick after stomp messages');
                this.appRef.tick();
            } catch (e) {
                console.warn('unknown error during stomp message app tick', e);
            }
        });
    }

    public sendMessage<T extends OpenposMessage>(message: T): boolean {
        if (message.type === MessageTypes.ACTION && message instanceof ActionMessage) {
            const actionMessage = message as ActionMessage;
            return this.publish(actionMessage.actionName, MessageTypes.SCREEN, actionMessage.payload, actionMessage.doNotBlockForResponse, actionMessage.lastKnownQueueSize);
        } else if (message.type === MessageTypes.PROXY && message instanceof ActionMessage) {
            const actionMessage = message as ActionMessage;
            return this.publish(actionMessage.actionName, actionMessage.type, actionMessage.payload, actionMessage.doNotBlockForResponse, actionMessage.lastKnownQueueSize);
        }
        this.sessionMessages$.next(message);
    }

    public getMessages(...types: string[]): Observable<any> {
        return merge(
            this.stompJsonMessages$,
            this.sessionMessages$
        ).pipe(
            filter(s => types && types.length > 0 ? types.includes(s.type) : true)
        );
    }

    public registerMessageHandler(handler: IMessageHandler<any>, ...types: string[]): Subscription {
        return this.getMessages(...types).subscribe(message => {

            // I don't think this zone needs to be included as Zone.js should already shimmed
            // both rxjs subscriptions and websockets, but I don't think it should be hurting
            // anything, so I'll leave it alone just in case.
            this.zone.run(() => handler.handle(message));
        });
    }

    public isRunningInBrowser(): boolean {
        const app = document.URL.indexOf('http://') === -1 && document.URL.indexOf('https://') === -1;
        return !app;
    }

    private buildTopicName(): string {
        return '/topic/app/device/' + this.personalization.getDeviceId$().getValue();
    }

    public setAuthToken(token: string) {
        this.authToken = token;
    }

    public addQueryParam(key: string, value: string) {
        this.queryParams[key] = value;
    }

    public connected(): boolean {
        return this.stompService && this.stompService.connected();
    }

    private appendPersonalizationProperties(headers: any) {
        const personalizationProperties = this.personalization.getPersonalizationProperties$().getValue();
        if (personalizationProperties && headers) {
            const keys = Array.from(personalizationProperties.keys());
            for (const key of keys) {
                headers[key] = personalizationProperties.get(key);
            }
        }
    }

    private getHeaders(): any {
        const headers = {
            authToken: this.authToken,
            deviceToken: this.personalization.getDeviceToken$().getValue(),
            compatibilityVersion: CONFIGURATION.compatibilityVersion,
            appId: this.personalization.getAppId$().getValue(),
            deviceId: this.personalization.getDeviceId$().getValue(),
            queryParams: JSON.stringify(this.queryParams),
            version: JSON.stringify(VERSIONS),
            powerStatus: this.powerStatus
        };
        this.appendPersonalizationProperties(headers);
        this.clientContexts.forEach(context => {
            const contextsToAdd = context.getContextProperties();
            contextsToAdd.forEach((value, key) => {
                headers[key] = value;
            });
        });
        return headers;
    }

    public async subscribe() {
        if (this.subscription) {
            return;
        }

        console.info(`Initiating session subscribe...`);
        const url: string = await this.negotiateWebsocketUrl();
        if (url) {
            console.info('creating new stomp service at: ' + url);
            this.stompUrl = url;

            this.connectToStomp();

            const currentTopic = this.buildTopicName();

            console.info('subscribing to server at: ' + currentTopic);

            const messages: Observable<Message> = this.stompService.subscribe(currentTopic);

            this.subscription = messages.subscribe((message: Message) => {
                console.info('Got STOMP message');
                if (this.inBackground) {
                    console.info('Leaving background');
                    this.inBackground = false;
                }
                if (this.isMessageVersionValid(message)) {
                    const json = JSON.parse(message.body);
                    this.logStompJson(json);
                    this.stompJsonMessages$.next(json);
                } else {
                    console.info(`Showing incompatible version screen`);
                    this.stompJsonMessages$.next(this.buildIncompatibleVersionScreen());
                }
            });

            this.state = this.stompService.state.pipe(map((state: number) => StompState[state]));

            if (!this.stompStateSubscription) {
                this.stompStateSubscription = this.state.subscribe(stompState => {
                    this.handleStompState(stompState);
                });
            }
        } else {
            console.error('Failed to negotiate server url');
        }

        if (!this.connected()) {
            this.sendDisconnected();
        }

    }

    public connectToStomp(): void {
        if (this.stompService.connected()) {
            this.stompService.disconnect();
        }
        this.stompService.config = {
            url: this.stompUrl,
            headers: this.getHeaders(),
            heartbeat_in: 0, // Typical value 0 - disabled
            heartbeat_out: 20000, // Typical value 20000 - every 20 seconds
            reconnect_delay: 250,  // Typical value is 5000, 0 disables.
            debug: this.stompDebug
        };

        this.stompService.initAndConnect();
    }

    private handleStompState(stompState: string) {
        if (stompState === 'CONNECTED') {
            this.reconnecting = false;
            this.connectedOnce = true;
            console.info('STOMP connecting', this.stompService);
            if (!this.onServerConnect.value) {
                this.onServerConnect.next(true);
            }

            this.sendMessage(new ConnectedMessage());

            // Screens will cancel the loading when they receive a message upon
            // connection, but just for sanity we're going to ensure it gets
            // cancelled after some due time. We need to let the screens handle
            // closing the dialog to prevent a user from executing actions before
            // the proper screen is displayed.
            timer(10000).pipe(take(1)).subscribe(() => {
                this.cancelLoading();
            });
        } else if (stompState === 'DISCONNECTING') {
            console.info('STOMP disconnecting');
        } else if (stompState === 'CLOSED') {
            console.info('STOMP closed');
            this.sendMessage(new SplashScreen('Reconnecting to server...'));
            this.sendMessage(new UnlockScreenMessage());
            this.sendDisconnected();
            if (!this.reconnecting) {
                this.renegotiateConnection();
            }
        }
    }

    private async negotiateWebsocketUrl(): Promise<string> {
        if (this.personalization.getIsManagedServer$().getValue()) {
            if (!this.discovery.getWebsocketUrl()) {
                const discoverResp = await this.discovery.discoverDeviceProcess();
                if (!discoverResp || !discoverResp.success) {
                    console.error(`Failed to get websocket url from OpenPOS Management Server. Reason: ` +
                        `${!!discoverResp ? discoverResp.message : 'unknown'}`);
                    return null;
                }
            }
        }
        return this.discovery.getWebsocketUrl();
    }

    private sendDisconnected() {
        if (this.connectedOnce) {
            this.sendMessage(new ImmediateLoadingMessage(this.disconnectedMessage));
            this.sendMessage(new DisconnectedMessage());
        }
    }

    handle(message: any) {
    }

    private logStompJson(json: any) {
        if (json && json.sequenceNumber && json.screenType) {
            console.info(`[logStompJson] type: ${json.type}, screenType: ${json.screenType}, seqNo: ${json.sequenceNumber}`);
        } else if (json) {
            console.info(`[logStompJson] type: ${json.type}`);
        } else {
            console.info(`[logStompJson] ${json}`);
        }
    }

    private buildIncompatibleVersionScreen(): any {
        return {
            type: 'Dialog',
            screenType: 'Dialog',
            template: { dialog: true, type: 'BlankWithBar' },
            dialogProperties: { closeable: false },
            title: 'Incompatible Versions',
            message: CONFIGURATION.incompatibleVersionMessage.split('\n')
        };
    }

    private isMessageVersionValid(message: Message): boolean {
        const valid = message.headers.compatibilityVersion === CONFIGURATION.compatibilityVersion;
        if (!valid) {
            console.info(`INCOMPATIBLE VERSIONS. Client compatibilityVersion: ${CONFIGURATION.compatibilityVersion}, ` +
                `server compatibilityVersion: ${message.headers.compatibilityVersion}`);
        }
        return valid;
    }

    public async ping(pingParams?: PingParams): Promise<PingResult> {
        let url = '';
        if (pingParams) {
            let protocol = 'http://';
            if (pingParams.useSsl) {
                protocol = 'https://';
            }
            url = protocol + pingParams.serverName;
            if (pingParams.serverPort) {
                url = url + ':' + pingParams.serverPort;
            }
            url = url + '/ping';

        } else {
            url = `${this.discovery.getServerBaseURL()}/ping`;
        }

        console.info('testing url: ' + url);

        let pingError: any = null;
        try {
            const httpResult = await this.http.get(url, {}).toPromise();
            if (httpResult) {
                console.info('successful validation of ' + url);
                return { success: true };
            } else {
                pingError = { message: '?' };
            }
        } catch (error) {
            pingError = error;
        }

        if (pingError) {
            console.info('bad validation of ' + url + ' with an error message of :' + pingError.message);
            return { success: false, message: pingError.message };
        }
    }

    private async renegotiateConnection() {
        if (this.reconnecting) {
            return;
        }
        if (this.personalization.getIsManagedServer$().getValue()) {
            this.unsubscribe();
            this.reconnecting = true;
            this.reconnectTimerSub = timer(5000, 5000).pipe(takeWhile(() => this.reconnecting)).subscribe(async () => {
                if (await this.discovery.isManagementServerAlive()) {
                    console.debug(`Management server is alive`);
                    if (this.discovery.getWebsocketUrl()) {
                        // TODO: May not be necessary to run in zone, check.
                        this.zone.run(() => {
                            this.subscribe();
                            this.reconnectTimerSub.unsubscribe();
                        });
                    }
                } else {
                    console.debug(`Management server is not alive`);
                }
            });
        }
    }

    public unsubscribe() {
        if (!this.subscription) {
            return;
        }

        console.info('unsubscribing from stomp service ...');

        // This will internally unsubscribe from Stomp Broker
        // There are two subscriptions - one created explicitly, the other created in the template by use of 'async'
        this.subscription.unsubscribe();
        this.subscription = null;

        this.stompStateSubscription.unsubscribe();
        this.stompStateSubscription = null;

        console.info('disconnecting from stomp service');
        this.stompService.disconnect();
        this.stompService.config = null;
    }

    public onDeviceResponse(deviceResponse: IDeviceResponse) {
        const sendResponseBackToServer = () => {
            // tslint:disable-next-line:max-line-length
            console.info(`>>> Publish deviceResponse requestId: "${deviceResponse.requestId}" deviceId: ${deviceResponse.deviceId} type: ${deviceResponse.type}`);
            this.stompService.publish(
                `/app/device/device/${this.personalization.getDeviceId$().getValue()}`,
                JSON.stringify(deviceResponse));
        };

        sendResponseBackToServer();
    }

    public keepAlive() {
        if (this.subscription) {
            console.info(`>>> KeepAlive`);
            this.publish('KeepAlive', 'KeepAlive');
        }
    }

    public setDisconnectedMessage(message: string) {
        if (message) {
            this.disconnectedMessage = message;
        } else {
            this.disconnectedMessage = LoaderState.DISCONNECTED_TITLE;
        }
    }

    public refreshScreen() {
        this.publish('Refresh', 'Screen');
    }

    public publish(actionString: string, type: string, payload?: any, doNotBlockForResponse = false, lastKnownQueueSize = 0): boolean {
        // Block any actions if we are backgrounded and running in cordova
        // (unless we are coming back out of the background)
        if (this.inBackground && actionString !== 'Refresh') {
            console.info(`Blocked action '${actionString}' because app is in background.`);
            return false;
        }
        const deviceId = this.personalization.getDeviceId$().getValue();
        if (deviceId) {
            console.info(`Publishing action '${actionString}' of type '${type}' to server...`);
            this.stompService.publish('/app/action/device/' + deviceId,
                JSON.stringify({ name: actionString, type, data: payload, doNotBlockForResponse, lastKnownQueueSize: lastKnownQueueSize }));
            return true;
        } else {
            console.info(`Can't publish action '${actionString}' of type '${type}' ` +
                `due to undefined Device ID (${deviceId})`);
            return false;
        }
    }

    public cancelLoading() {
        this.sendMessage(new CancelLoadingMessage());
    }

    public getCurrencyDenomination(): string {
        return 'USD';
    }

    public sendPowerStatus(status: PowerStatus) {
        this.powerStatus = status;
        this.sendMessage(new ActionMessage('PowerStatusChanged', true, status));
    }
}
