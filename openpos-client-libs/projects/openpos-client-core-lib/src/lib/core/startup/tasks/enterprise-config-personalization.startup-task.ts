import { Injectable } from '@angular/core';
import { take } from 'rxjs/operators';
import { PersonalizationService } from '../../personalization/personalization.service';
import { AutoPersonalizationStartupTask } from './auto-personalization.startup-task';
import { EnterpriseConfigService } from '../../platform-plugins/enterprise-config/enterprise-config.service';
import { AppPlatformService } from '../../services/app-platform.service';
import { SavedSessionPersonalizationStartupTask } from './saved-session-personalization.startup-task';

@Injectable({
    providedIn: 'root'
})

export class EnterpriseConfigPersonalizationStartupTask extends AutoPersonalizationStartupTask {
    private static readonly AUTO_PERSONALIZE_URL_PROPERTY_NAME = 'jmc_autoPersonalizeUrl';
    name = 'EnterpriseConfigPersonalizationStartupTask';

    constructor(personalization: PersonalizationService, private enterpriseConfigService: EnterpriseConfigService,
                private appPlatform: AppPlatformService, private savedSessionTask: SavedSessionPersonalizationStartupTask) {
        super(personalization);
    }

    async execute(): Promise<void> {
        const skip = await this.personalization.getSkipAutoPersonalization$().pipe(
            take(1)
        ).toPromise();

        if (skip) {
            console.debug('skip auto-personalization requested');
            throw new Error('skip auto-personalization requested');
        }
        const config = this.enterpriseConfigService.getConfiguration();
        const autoPersonalizeViaConfigEnabled = config?.hasOwnProperty(
            EnterpriseConfigPersonalizationStartupTask.AUTO_PERSONALIZE_URL_PROPERTY_NAME
        );

        if (autoPersonalizeViaConfigEnabled && this.personalization.hasSavedSession()) {
            console.info('Saved session detected, auto-personalization via enterprise config not necessary.');
            console.info('Running saved session personalization...');
            return this.savedSessionTask.execute();
        }

        if (autoPersonalizeViaConfigEnabled) {
            const url = config[EnterpriseConfigPersonalizationStartupTask.AUTO_PERSONALIZE_URL_PROPERTY_NAME];

            console.info(`Attempting auto-personalization using URL from Enterprise Configuration: ${url}`);
            const deviceName = await this.getDeviceName();
            return this.personalize({deviceName, additionalAttributes: config}, url);
        } else {
            throw new Error(Object.keys(config).length === 0 ?
                `No Enterprise config found, skipping auto personalization with Enterprise config` :
                `${EnterpriseConfigPersonalizationStartupTask.AUTO_PERSONALIZE_URL_PROPERTY_NAME} not found in enterprise config: ${JSON.stringify(config)}`);
        }
    }

    async getDeviceName(): Promise<string> {
        return this.appPlatform.getDeviceName().toPromise();
    }
}
