import { Injectable } from '@angular/core';
import { StatusMessage } from '../../shared/status/status.message';
import { MessageTypes } from '../messages/message-types';
import { Status } from '../messages/status.enum';
import { PersonalizationService } from '../personalization/personalization.service';
import { ServerLocation } from '../personalization/server-location';
import { SessionService } from './session.service';
import { delay, takeUntil, filter, skipWhile, withLatestFrom, map } from 'rxjs/operators';
import { Subject, interval, Observable } from 'rxjs';


@Injectable({
    providedIn: 'root'
})
export class FailoverService {
    private readonly destroyServerTimeout$ = new Subject();
    private readonly destroyRestore$ = new Subject();
    private screenAllowsRestore$: Observable<boolean>;

    constructor(private session: SessionService, private personalization: PersonalizationService) {
        session.getMessages(MessageTypes.CONFIG_CHANGED).pipe(filter(m => m.configType === 'failover')).subscribe(m => this.configure(m));
        this.screenAllowsRestore$ = session.getMessages(MessageTypes.SCREEN).pipe(map(m => m.allowRestoreFromFailover));
    }

    public isFailedOver(): boolean {
        const primaryServer = this.personalization.getPrimaryServer();
        if (primaryServer) {
            return !primaryServer.active;
        } else {
            return false;
        }
    }

    private configure(m: any) {
        console.log(`Failover timeout: ${m.serverTimeout}, restoreCheckInterval ${m.restoreCheckInterval}`);
        this.destroyServerTimeout$.next(true);
        this.destroyRestore$.next(true);

        this.session.getMessages(MessageTypes.DISCONNECTED)
            .pipe(delay(m.serverTimeout), takeUntil(this.destroyServerTimeout$)).subscribe(() => this.failover());
        interval(m.restoreCheckInterval)
            .pipe(
                withLatestFrom(this.screenAllowsRestore$),
                skipWhile(([i, allowed]) => !this.isFailedOver() || !allowed),
                takeUntil(this.destroyRestore$)).subscribe(() => this.tryRestorePrimary()
                );

        if (this.isFailedOver()) {
            console.log('Sending Failover Status message');
            this.session.sendMessage(new StatusMessage('1', 'Failover', m.statusIconName, Status.OFFLINE, m.statusOfflineMessage));
        }
    }

    private async failover() {

        // Once we failover pause the timeout check
        this.destroyServerTimeout$.next(true);

        if (!this.personalization.getFailovers() || this.personalization.getFailovers().length < 1) {
            console.log('No failover severs are set');
            return;
        }

        let i = this.personalization.getFailovers().findIndex(value => value.active);
        i += 1;
        while (
            i < this.personalization.getFailovers().length &&
            !(await this.testLocationOnline(this.personalization.getFailovers()[i]))
        ) {
            ++i;
        }

        if (i < this.personalization.getFailovers().length) {
            const failoverLocation = this.personalization.getFailovers()[i];

            if (!!failoverLocation.token) {
                console.log(`Failing over to ${failoverLocation.address}:${failoverLocation.port}`);
                this.personalization.personalize({
                    serverConnection: {
                        host: failoverLocation.address,
                        port: +failoverLocation.port,
                        secured: this.personalization.getSslEnabled$().getValue()
                    },
                    authToken: failoverLocation.token,
                    deviceId: this.personalization.getDeviceId$().getValue(),
                    appId: this.personalization.getAppId$().getValue(),
                    params: this.personalization.getPersonalizationProperties$().getValue(),
                }).subscribe(result => {
                    console.log(result);
                    this.personalization.refreshApp();
                });
            } else {
                console.log(`Failing over to ${failoverLocation.address}:${failoverLocation.port} DeviceId ${this.personalization.getDeviceId$().getValue()}`);
                this.personalization.personalize({
                    serverConnection: {
                        host: failoverLocation.address,
                        port: +failoverLocation.port,
                        secured: this.personalization.getSslEnabled$().getValue()
                    },
                    deviceId: this.personalization.getDeviceId$().getValue(),
                    appId: this.personalization.getAppId$().getValue(),
                    params: this.personalization.getPersonalizationProperties$().getValue()
                }).subscribe(result => {
                    console.log(result);
                    this.personalization.refreshApp();
                });
            }
        }

    }

    private async tryRestorePrimary() {
        const location = this.personalization.getPrimaryServer();
        if (await this.testLocationOnline(location)) {
            if (location.token) {
                console.log(`Restoring to primary server ${location.address}:${location.port}`);
                this.personalization.personalizeWithToken(
                    location.address,
                    location.port,
                    location.token,
                    this.personalization.getSslEnabled$().getValue()
                ).subscribe(result => {
                    console.log(result);
                    this.personalization.refreshApp();
                });
            } else {
                console.log(`Restoring to primary server ${location.address}:${location.port}`);
                this.personalization.personalize({
                    serverConnection: {
                        host: location.address,
                        port: +location.port,
                        secured: this.personalization.getSslEnabled$().getValue()
                    },
                    deviceId: this.personalization.getDeviceId$().getValue(),
                    appId: this.personalization.getAppId$().getValue(),
                    params: this.personalization.getPersonalizationProperties$().getValue()
                }).subscribe(result => {
                    console.log(result);
                    this.personalization.refreshApp();
                });
            }
        }
    }

    private async testLocationOnline(location: ServerLocation): Promise<boolean> {
        const result = await this.session.ping({
            serverName: location.address,
            serverPort: location.port,
            useSsl: this.personalization.getSslEnabled$().getValue()
        });

        return result.success;
    }
}
