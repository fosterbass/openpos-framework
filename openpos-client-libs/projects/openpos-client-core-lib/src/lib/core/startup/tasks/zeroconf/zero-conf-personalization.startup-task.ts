import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BehaviorSubject, merge, timer } from 'rxjs';
import { first, last, map, take, tap, timeout } from 'rxjs/operators';
import { ConfigChangedMessage } from '../../../messages/config-changed-message';
import { AutoPersonalizationParametersResponse } from '../../../personalization/device-personalization.interface';
import { PersonalizationService } from '../../../personalization/personalization.service';
import { ConfigurationService } from '../../../services/configuration.service';
import { Zeroconf, ZeroconfService } from '../../../zeroconf/zeroconf';
import { AutoPersonalizationStartupTask } from '../auto-personalization.startup-task';
import { ZeroConfPersonalizationDialogComponent } from './zero-conf-personalization-dialog.component';

class ZeroConfPersonalizationConfig extends ConfigChangedMessage {
    enabled?: boolean;
    searchDomains?: string[];

    constructor() {
        super('zeroconf');
    }
}

@Injectable({
    providedIn: 'root'
})
export class ZeroConfPersonalizationStartupTask extends AutoPersonalizationStartupTask {
    private static readonly TYPE = '_jmc-personalize._tcp.';

    static readonly taskName = 'ZeroConfPersonalizationStartupTask';

    constructor(
        private _config: ConfigurationService,
        private _dialog: MatDialog,
        personalization: PersonalizationService
    ) {
        super(personalization);
    }

    async execute(): Promise<void> {
        const skip = await this.personalization.getSkipAutoPersonalization$().pipe(
            take(1)
        ).toPromise();

        if (skip) {
            console.debug('skip auto-personalization requested');
            throw new Error('skip personalization requested');
        }

        let provider: Zeroconf;

        try {
            provider = await this.personalization.getAutoPersonalizationProvider$().pipe(
                last()
            ).toPromise();
        } catch (e) {
            throw new Error('no zeroconf providers found');
        }

        const currentStep = new BehaviorSubject(0);
        const statusDialog = this._dialog.open(ZeroConfPersonalizationDialogComponent, {
            panelClass: 'openpos-default-theme',
            hasBackdrop: false,
            disableClose: true,
            data: currentStep.asObservable()
        });

        let keepTrying = true;
        statusDialog.afterClosed().subscribe(value => {
            if (typeof value === 'boolean' && !value) {
                keepTrying = false;
            }
        });

        let searchDomains = ['local'];

        const config = this._config.peekConfiguration<ZeroConfPersonalizationConfig>('zeroconf');
        if (config) {
            const enabled = config.enabled ?? true;
            if (!enabled) {
                throw new Error('configuration disabled zeroconf personalization');
            }

            if (config.searchDomains && config.searchDomains.length > 0) {
                searchDomains = config.searchDomains;
            }
        }

        console.debug('searching domains [' + searchDomains.join(', ') + '] for personalization service');

        let backOffTime = 1000;

        let service: ZeroconfService;

        while (keepTrying) {
            try {
                service = await merge(...searchDomains.map(domain => {
                    return provider.watch(ZeroConfPersonalizationStartupTask.TYPE, domain);
                })).pipe(
                    first(r => r.action === 'resolved'),
                    tap(r => console.log('ZeroConf Service Resolution', r.service)),
                    map(r => r.service),
                    timeout(10000)
                ).toPromise();

                // if we got here we successfully have a service; break the loop
                break;
            } catch (e) {
                if (!!e.name && e.name === 'TimeoutError') {
                    console.warn(`timeout experienced while searching for ${ZeroConfPersonalizationStartupTask.TYPE} services.`);
                } else {
                    console.error('unknown error occurred while collecting services', e);
                }
            }

            await timer(backOffTime).pipe(take(1)).toPromise();
            backOffTime = Math.min(10000, backOffTime * 2);
        }

        backOffTime = 1000;

        currentStep.next(1);

        let deviceName: string;

        while (keepTrying) {
            try {
                deviceName = await provider.deviceName().pipe(
                    first()
                ).toPromise();

                // if we got here, we successfully have the device name; break the loop
                break;
            } catch (e) {
                console.error('unknown error while attempting to get the device name', e);
            }

            await timer(backOffTime).pipe(take(1)).toPromise();
            backOffTime = Math.min(30000, backOffTime * 2);
        }

        backOffTime = 1000;

        // generate the url where personalization parameters will be discovered.
        const endpoint = `${service.ipv4Addresses[0]}:${service.port}/${service.txtRecord.path}`;

        function booleanParameter(value?: boolean | string): boolean {
            if (typeof value === 'string') {
                return value.trim().toLowerCase() === 'true';
            } else if (typeof value === 'boolean') {
                return value;
            }

            return false;
        }

        // if the secured flag is on, then used https. Value is a string.
        const useHttps = booleanParameter(service.txtRecord.secured);

        let info: AutoPersonalizationParametersResponse;

        while (keepTrying) {
            try {
                info = await this.getPersonalizationParameters(deviceName, (useHttps ? 'https' : 'http') + '://' + endpoint);
                break;
            } catch (e) {
                console.error('error while attempting to get personalization parameters', e);
            }

            await timer(backOffTime).pipe(take(1)).toPromise();
            backOffTime = Math.min(30000, backOffTime * 2);
        }

        backOffTime = 1000;

        currentStep.next(2);

        while (keepTrying) {
            try {
                await this.personalize(info);

                statusDialog.close(true);

                // we've successfully personalized... keep going with the startup sequence
                return;
            } catch (e) {
                console.error('failed to personalize with server', e);
            }

            await timer(backOffTime).pipe(take(1)).toPromise();
            backOffTime = Math.min(30000, backOffTime * 2);
        }

        throw new Error('user requested to skip auto personalization using zeroconf');
    }
}
