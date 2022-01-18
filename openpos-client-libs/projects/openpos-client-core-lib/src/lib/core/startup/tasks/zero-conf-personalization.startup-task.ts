import { Injectable } from '@angular/core';
import { first, last, take, timeout } from 'rxjs/operators';
import { PersonalizationService } from '../../personalization/personalization.service';
import { Zeroconf, ZeroconfResult } from '../../zeroconf/zeroconf';
import { AutoPersonalizationStartupTask } from './auto-personalization.startup-task';
import { AppPlatformService } from '../../services/app-platform.service';
import { CONFIGURATION } from '../../../configuration/configuration';

@Injectable({
    providedIn: 'root'
})
export class ZeroConfPersonalizationStartupTask extends AutoPersonalizationStartupTask {
    private static readonly TYPE = '_jmc-personalize._tcp.';
    private static readonly DOMAIN = '';
    name = 'ZeroConfPersonalizationStartupTask';

    constructor(personalization: PersonalizationService, private appPlatform: AppPlatformService) {
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

        let service: ZeroconfResult;

        try {
            service = await provider.watch(
                ZeroConfPersonalizationStartupTask.TYPE,
                ZeroConfPersonalizationStartupTask.DOMAIN
            ).pipe(
                first(r => r.action === 'resolved'),
                timeout(10000)
            ).toPromise();
        } catch (e) {
            throw new Error('service discovery failed to locate viable service');
        }

        let deviceName: string;

        try {
            deviceName = await this.appPlatform.getDeviceName().pipe(
                first()
            ).toPromise();
        } catch (e) {
            throw new Error('failed to get required device name for personalization');
        }

        await this.personalize({deviceName}, CONFIGURATION.autoPersonalizationServicePath);
    }
}
