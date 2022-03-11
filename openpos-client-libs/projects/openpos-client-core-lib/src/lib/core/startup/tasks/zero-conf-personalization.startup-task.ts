import { Injectable } from '@angular/core';
import { first, last, map, take, timeout } from 'rxjs/operators';
import { PersonalizationService } from '../../personalization/personalization.service';
import { Zeroconf, ZeroconfService } from '../../zeroconf/zeroconf';
import { AutoPersonalizationStartupTask } from './auto-personalization.startup-task';

@Injectable({
    providedIn: 'root'
})
export class ZeroConfPersonalizationStartupTask extends AutoPersonalizationStartupTask {
    private static readonly TYPE = '_jmc-personalize._tcp.';
    private static readonly DOMAIN = '';

    constructor(personalization: PersonalizationService) {
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

        let service: ZeroconfService;

        try {
            service = await provider.watch(
                ZeroConfPersonalizationStartupTask.TYPE,
                ZeroConfPersonalizationStartupTask.DOMAIN
            ).pipe(
                first(r => r.action === 'resolved'),
                map(r => r.service),
                timeout(10000)
            ).toPromise();
        } catch (e) {
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
