import { Injectable, Inject, Optional } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PersonalizationConfigResponse } from './personalization-config-response.interface';
import { BehaviorSubject, Observable, throwError, zip, merge, concat, of } from 'rxjs';
import { catchError, filter, map, retry, take, tap, timeout } from 'rxjs/operators';
import { PersonalizationRequest } from './personalization-request';
import { PersonalizationResponse } from './personalization-response.interface';
import { AutoPersonalizationParametersResponse } from './device-personalization.interface';
import { CONFIGURATION } from '../../configuration/configuration';

import { Storage } from '../storage/storage.service';
import { Zeroconf, ZEROCONF_TOKEN } from '../zeroconf/zeroconf';
import { ServerLocation } from './server-location';
import { AutoPersonalizationRequest } from './auto-personalization-request.interface';

@Injectable({
    providedIn: 'root'
})
export class PersonalizationService {
    static readonly OPENPOS_MANAGED_SERVER_PROPERTY = 'managedServer';

    readonly personalizationInitialized$ = new BehaviorSubject<boolean>(false);

    private readonly personalizationProperties$ = new BehaviorSubject<Map<string, string> | null>(null);
    private readonly deviceId$ = new BehaviorSubject<string | null>(null);
    private readonly appId$ = new BehaviorSubject<string | null>(null);
    private readonly deviceToken$ = new BehaviorSubject<string | null>(null);
    private readonly serverName$ = new BehaviorSubject<string | null>(null);
    private readonly serverPort$ = new BehaviorSubject<string | null>(null);
    private readonly sslEnabled$ = new BehaviorSubject<boolean | null>(null);
    private readonly isManagedServer$ = new BehaviorSubject<boolean | null>(null);
    private readonly skipAutoPersonalization$ = new BehaviorSubject<boolean | null>(null);
    public readonly personalizationSuccessFul$ = new BehaviorSubject<boolean>(false);
    private failovers;
    private primaryServer;

    constructor(
        private storage: Storage,
        private http: HttpClient,
        @Inject(ZEROCONF_TOKEN) @Optional() protected mdns: Array<Zeroconf>
    ) {
        zip(
            storage.getValue('deviceToken'),
            storage.getValue('serverName'),
            storage.getValue('serverPort'),
            storage.getValue('sslEnabled'),
            storage.getValue(PersonalizationService.OPENPOS_MANAGED_SERVER_PROPERTY),
            storage.getValue('skipAutoPersonalization'),
            storage.getValue('primaryServerName'),
            storage.getValue('primaryServerPort'),
            storage.getValue('primaryServerToken'),
            storage.getValue('failover0ServerName'),
            storage.getValue('failover0Port'),
            storage.getValue('failover0Token'),
            storage.getValue('failover1ServerName'),
            storage.getValue('failover1Port'),
            storage.getValue('failover1Token'),
            storage.getValue('failover2ServerName'),
            storage.getValue('failover2Port'),
        ).subscribe(results => {
            console.log('Storage results', results);
            if (results[0]) {
                const deviceToken = results[0] !== 'null' ? results[0] : null;
                this.deviceToken$.next(deviceToken);
            }

            if (results[1]) {
                const serverName = results[1] !== 'null' ? results[1] : null;
                this.serverName$.next(serverName);
            }

            if (results[2]) {
                const serverPort = results[2] !== 'null' ? results[2] : null;
                this.serverPort$.next(serverPort);
            }

            if (results[3]) {
                this.sslEnabled$.next(results[3] === 'true');
            }

            if (results[4]) {
                this.isManagedServer$.next(results[4] === 'true');
            }

            if (results[5]) {
                this.skipAutoPersonalization$.next(results[5] === 'true');
            } else {
                this.skipAutoPersonalization$.next(false);
            }

            if (results[6] && results[7]) {
                this.primaryServer = new ServerLocation(results[6], results[7], results[8]);
            }

            const failovers: ServerLocation[] = [];

            if (results[9] && results[10]) {
                const location = new ServerLocation(results[9], results[10], results[11]);

                failovers.push(location);
            }

            if (results[12] && results[13]) {
                const location = new ServerLocation(results[12], results[13], results[14]);

                failovers.push(location);
            }

            if (results[15] && results[16]) {
                const location = new ServerLocation(results[15], results[16], results[17]);

                failovers.push(location);
            }

            if (failovers.length > 0) {
                console.log('Found failover servers');
                this.failovers = failovers;
            } else {
                console.log('No failover servers set');
            }

            this.personalizationInitialized$.next(true);
        });
    }

    public getAutoPersonalizationProvider$(): Observable<Zeroconf> {
        return merge(...this.mdns.map(mdn => mdn.isAvailable().pipe(
            take(1),
            map(avail => ({ provider: mdn, avail })),
            filter(m => m.avail),
            map(m => m.provider)
        )));
    }

    public getAutoPersonalizationParameters(request: AutoPersonalizationRequest, url: string):
        Observable<AutoPersonalizationParametersResponse> {

        console.log('Attempting auto personalization with ' + url);
        const protocol = this.sslEnabled$.getValue() ? 'https://' : 'http://';
        url = protocol + url;

        const requestTimeout = 2000;
        const retryCount = CONFIGURATION.autoPersonalizationRequestTimeoutMillis / requestTimeout;

        return this.http.put<AutoPersonalizationParametersResponse>(url, request)
            .pipe(
                timeout(requestTimeout),
                retry(retryCount),
                tap(response => {
                    if (response) {
                        console.info(`Auto personalization response received: ${JSON.stringify(response)}`);
                        response.sslEnabled = this.sslEnabled$.getValue();
                        this.setFailovers(response.failoverAddresses);
                        this.setPrimaryServer(new ServerLocation(response.serverAddress, response.serverPort, null));
                    }
                }));
    }

    public personalizeFromSavedSession(): Observable<string> {
        const request = new PersonalizationRequest(this.deviceToken$.getValue(), null, null, null);
        return this.sendPersonalizationRequest(
            this.sslEnabled$.getValue(),
            this.serverName$.getValue(),
            this.serverPort$.getValue(),
            request,
            null
        );
    }

    public hasSavedSession(): boolean {
        return !!this.deviceToken$.getValue() && !!this.serverPort$.getValue() && !!this.serverName$.getValue();
    }

    public personalize(
        serverName: string,
        serverPort: string,
        deviceId: string,
        appId: string,
        personalizationProperties?: Map<string, string>,
        sslEnabled?: boolean
    ): Observable<string> {

        const request = new PersonalizationRequest(this.deviceToken$.getValue(), deviceId, appId, null);
        return this.sendPersonalizationRequest(sslEnabled, serverName, serverPort, request, personalizationProperties);
    }

    public personalizeWithToken(
        serverName: string,
        serverPort: string,
        deviceToken: string,
        sslEnabled?: boolean,
        pairedDeviceId?: string
    ): Observable<string> {
        const request = new PersonalizationRequest(deviceToken, null, null, null, pairedDeviceId);
        return this.sendPersonalizationRequest(sslEnabled, serverName, serverPort, request, null);
    }

    private sendPersonalizationRequest(
        sslEnabled: boolean,
        serverName: string,
        serverPort: string,
        request: PersonalizationRequest,
        personalizationParameters: Map<string, string>
    ): Observable<string> {
        let url = sslEnabled ? 'https://' : 'http://';
        url += serverName + ':' + serverPort + '/rest/devices/personalize';

        if (personalizationParameters) {
            console.log('personalizationParams', personalizationParameters);
            personalizationParameters.forEach((value, key) => request.personalizationParameters[key] = value);
        }

        console.info(`[PersonalizationService] sending personalizationRequest: ${JSON.stringify(request)}, to url: ${url}`);
        return this.http.post<PersonalizationResponse>(url, request).pipe(
            map((response: PersonalizationResponse) => {
                console.info(`personalizing with server: ${serverName}, port: ${serverPort}, deviceId: ${request.deviceId}`);
                this.setServerName(serverName);
                this.setServerPort(serverPort);
                this.setDeviceId(response.deviceModel.deviceId);
                this.setDeviceToken(response.authToken, serverName, serverPort);
                this.setAppId(response.deviceModel.appId);
                if (!personalizationParameters) {
                    personalizationParameters = new Map<string, string>();
                }
                if (response.deviceModel.deviceParamModels) {
                    response.deviceModel.deviceParamModels.forEach(value => {
                        personalizationParameters.set(value.paramName, value.paramValue);
                    });
                }

                this.setPersonalizationProperties(personalizationParameters);

                if (sslEnabled) {
                    this.setSslEnabled(sslEnabled);
                } else {
                    this.setSslEnabled(false);
                }

                if (this.primaryServer) {
                    this.primaryServer.active = this.primaryServer.address === this.serverName$.getValue() &&
                        this.primaryServer.port === this.serverPort$.getValue();
                }

                if (this.failovers) {
                    this.failovers.forEach(f => {
                        f.active = f.address === this.serverName$.getValue() && f.port === this.serverPort$.getValue();
                    });
                }

                this.personalizationSuccessFul$.next(true);
                return 'Personalization successful';
            }),
            catchError(error => {
                console.log(error);
                this.personalizationSuccessFul$.next(false);
                if (error.status === 401) {
                    return throwError(`Device saved token does not match server`);
                }

                if (error.status === 0) {
                    return throwError(`Unable to connect to ${serverName}:${serverPort}`);
                }

                return throwError(`${error.statusText}`);
            })
        );
    }

    public dePersonalize() {
        this.remove(this.deviceId$, null);
        this.remove(this.appId$, null);
        this.remove(this.serverName$, this.storage.remove('serverName'));
        this.remove(this.serverPort$, this.storage.remove('serverPort'));
        this.remove(this.deviceToken$, this.storage.remove('deviceToken'));
        this.remove(null, this.storage.remove('theme'));
        this.remove(this.sslEnabled$, this.storage.remove('sslEnabled'));
        this.remove(this.skipAutoPersonalization$, this.storage.remove('skipAutoPersonalization'));
        this.remove(this.isManagedServer$, this.storage.remove(PersonalizationService.OPENPOS_MANAGED_SERVER_PROPERTY));
        this.remove(this.personalizationProperties$, null);
    }


    public requestPersonalizationConfig(
        serverName: string,
        serverPort: string,
        sslEnabled: boolean
    ): Observable<PersonalizationConfigResponse> {
        let url = sslEnabled ? 'https://' : 'http://';
        url += serverName + ':' + serverPort + '/rest/devices/personalizationConfig';

        console.log('Requesting Personalization config with url: ' + url);
        return this.http.get<PersonalizationConfigResponse>(url).pipe(
            tap(result => result ? console.log('Successful retrieval of Personalization Config with url: ' + url) : null)
        );
    }

    public getPersonalizationProperties$(): BehaviorSubject<Map<string, string>> {
        return this.personalizationProperties$;
    }

    public getSslEnabled$(): BehaviorSubject<boolean> {
        return this.sslEnabled$;
    }

    public getServerName$(): BehaviorSubject<string> {
        return this.serverName$;
    }

    public getServerPort$(): BehaviorSubject<string> {
        return this.serverPort$;
    }

    public getDeviceId$(): BehaviorSubject<string> {
        return this.deviceId$;
    }

    public getAppId$(): BehaviorSubject<string> {
        return this.appId$;
    }

    public getDeviceToken$(): BehaviorSubject<string> {
        return this.deviceToken$;
    }

    public getIsManagedServer$(): BehaviorSubject<boolean> {
        return this.isManagedServer$;
    }

    public getPersonalizationSuccessful$(): BehaviorSubject<boolean> {
        return this.personalizationSuccessFul$;
    }

    public getSkipAutoPersonalization$(): Observable<boolean> {
        return this.skipAutoPersonalization$;
    }

    public getFailovers(): ServerLocation[] {
        return this.failovers;
    }

    public getPrimaryServer(): ServerLocation {
        return this.primaryServer;
    }

    public setPersonalizationProperties(personalizationProperties?: Map<string, string>) {
        this.personalizationProperties$.next(personalizationProperties);
    }

    private setSslEnabled(enabled: boolean) {
        this.storage.setValue('sslEnabled', enabled + '').subscribe();
        this.sslEnabled$.next(enabled);
    }

    private setServerName(name: string) {
        this.storage.setValue('serverName', name).subscribe();
        this.serverName$.next(name);
    }

    private setServerPort(port: string) {
        this.storage.setValue('serverPort', port).subscribe();
        this.serverPort$.next(port);
    }

    private setDeviceId(id: string) {
        this.deviceId$.next(id);
    }

    private setAppId(id: string) {
        this.appId$.next(id);
    }

    private setDeviceToken(token: string, server: string, port: string) {
        this.storage.setValue('deviceToken', token).subscribe();

        // Persist token with correct server address
        const primaryServerLocation = this.primaryServer;
        if (!!primaryServerLocation && primaryServerLocation.address === server && primaryServerLocation.port === port) {
            this.setPrimaryServer(new ServerLocation(server, port, token));
        } else if (!!this.failovers && this.failovers.size > 0) {
            this.failovers.forEach(failover => {
                if (failover.address === server && failover.port === port) {
                    failover.token = token;
                }
            });
        }

        this.deviceToken$.next(token);
    }

    private setFailovers(failovers: ServerLocation[]) {
        if (failovers) {
            failovers.forEach((value, index) => {
                console.log('Recording failover server ' + index + ': ' + value.address + ':' + value.port + ' ' + value.token);
                this.storage.setValue(`failover${index}ServerName`, value.address);
                this.storage.setValue(`failover${index}Port`, value.port);
                this.storage.setValue(`failover${index}Token`, value.token);
            });
        }

        this.failovers = failovers;
    }

    private setPrimaryServer(primary: ServerLocation) {
        this.storage.setValue(`primaryServerName`, primary.address);
        this.storage.setValue(`primaryServerPort`, primary.port);
        this.storage.setValue(`primaryServerToken`, primary.token);
    }

    public setSkipAutoPersonalization(skip: boolean) {
        if (skip) {
            this.storage.setValue('skipAutoPersonalization', 'true').subscribe();
        } else {
            this.storage.remove('skipAutoPersonalization').subscribe();
        }

        this.skipAutoPersonalization$.next(skip);
    }

    public refreshApp() {
        window.location.reload();
    }

    public clearStorage(): Observable<void> {
        return concat(
            this.storage.clear(),
            of(this.remove(this.deviceId$, null)),
            of(this.remove(this.serverName$, null)),
            of(this.remove(this.serverPort$, null)),
            of(this.remove(this.deviceToken$, null)),
            of(this.remove(this.sslEnabled$, null)),
            of(this.remove(this.isManagedServer$, null)),
            of(this.remove(this.skipAutoPersonalization$, null)),
            of(this.remove(this.personalizationProperties$, null))
        );
    }

    private remove(fieldSubject: BehaviorSubject<any>, storageField: Observable<void>) {
        if (fieldSubject) {
            fieldSubject.next(null);
        }
        if (storageField) {
            storageField.subscribe();
        }
    }
}
