import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { last } from 'rxjs/operators';
import { PersonalizationService } from '../../personalization/personalization.service';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class QueryParamsPersonalization implements StartupTask {
    static readonly taskName = 'QueryParamsPersonalization';

    constructor(
        private _activatedRoute: ActivatedRoute,
        private _personalization: PersonalizationService
    ) { }

    async execute(): Promise<void> {
        const queryParams = this._activatedRoute.snapshot.queryParams;

        const deviceId = queryParams.deviceId;
        const appId = queryParams.appId;
        const serverName = queryParams.serverName;
        const deviceToken = queryParams.deviceToken;
        const pairedDeviceId = queryParams.pairedDeviceId;
        let serverPort = queryParams.serverPort;
        let sslEnabled = queryParams.sslEnabled === 'true';

        const personalizationProperties = new Map<string, string>(
            Object.keys(queryParams)
                .filter(ok => !['deviceId', 'deviceToken', 'serverName', 'serverPort', 'sslEnabled'].includes(ok))
                .map(ok => [ok, queryParams[ok]])
        );

        serverPort = serverPort ?? 6140;
        sslEnabled = sslEnabled ?? false;

        if (deviceToken && serverName) {
            await this._personalization.personalizeWithToken(serverName, serverPort, deviceToken, sslEnabled, pairedDeviceId)
                .pipe(last())
                .toPromise();
            return;
        }

        if (deviceId && serverName) {
            await this._personalization.personalize(
                serverName,
                serverPort,
                deviceId,
                appId,
                personalizationProperties,
                sslEnabled
            ).pipe(last()).toPromise();
            return;
        }

        throw new Error('failed to personalize using query params');
    }
}
