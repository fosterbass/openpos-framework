import { Injectable } from '@angular/core';
import { merge } from 'rxjs';
import { first, last, map, take, tap, timeout } from 'rxjs/operators';
import { ConfigChangedMessage } from '../../messages/config-changed-message';
import { PersonalizationService } from '../../personalization/personalization.service';
import { ConfigurationService } from '../../services/configuration.service';
import { Zeroconf, ZeroconfService } from '../../zeroconf/zeroconf';
import { AutoPersonalizationStartupTask } from './auto-personalization.startup-task';

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

        let service: ZeroconfService;

        try {
            service = await merge(...searchDomains.map(domain => {
                return provider.watch(ZeroConfPersonalizationStartupTask.TYPE, domain);
            })).pipe(
                first(r => r.action === 'resolved'),
                tap(r => console.log('ZeroConf Service Resolution', r.service)),
                map(r => r.service),
                timeout(10000)
            ).toPromise();
        } catch (e) {
            if (!!e.name && e.name === 'TimeoutError') {
                console.warn(`timeout experienced while searching for ${ZeroConfPersonalizationStartupTask.TYPE} services.`);
            } else {
                console.error('unknown error occurred while collecting services', e);
            }

            throw new Error('service discovery failed to locate viable service');
        }

        let deviceName: string;

        try {
            deviceName = await provider.deviceName().pipe(
                first()
            ).toPromise();
        } catch (e) {
            throw new Error('failed to get required device name for personalization');
        }

        // generate the url where personalization parameters will be discovered.
        const endpoint = `${service.ipv4Addresses[0]}:${service.port}/${service.txtRecord.path}`;

        function booleanParameter(value?: boolean | string): boolean {
            if (typeof value === 'string') {
                return value.trim().toLocaleLowerCase() === 'true';
            } else if (typeof value === 'boolean') {
                return value;
            }

            return false;
        }

        // if the secured flag is on, then used https. Value is a string.
        const useHttps = booleanParameter(service.txtRecord.secured);

        await this.personalize(deviceName, (useHttps ? 'https' : 'http') + '://' + endpoint);
    }
}
