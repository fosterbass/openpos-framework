import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { CONFIGURATION } from '../../../configuration/configuration';
import { AutoPersonalizationParametersResponse } from '../../personalization/device-personalization.interface';
import { PersonalizationService } from '../../personalization/personalization.service';
import { StartupTask } from '../startup-task';

export abstract class AutoPersonalizationStartupTask implements StartupTask {
    constructor(protected personalization: PersonalizationService) { }

    abstract execute(): void | Promise<void> | Observable<any>;

    protected async personalize(deviceName: string): Promise<void> {
        let info: AutoPersonalizationParametersResponse;

        try {
            info = await this.personalization.getAutoPersonalizationParameters(
                deviceName,
                CONFIGURATION.autoPersonalizationServicePath
            ).pipe(
                first()
            ).toPromise();
        } catch (e) {
            throw new Error('failed to get personalization parameters');
        }

        console.log(`personalizing with server '${info.serverAddress}:${info.serverPort}' as '${info.deviceName}'`);

        const params = info.personalizationParams;
        let paramsMap: Map<string, string>;

        if (params) {
            paramsMap = new Map<string, string>();

            for (const key in params) {
                if (key) {
                    paramsMap.set(key, params[key]);
                }
            }
        }

        try {
            await this.personalization.personalize(
                info.serverAddress,
                info.serverPort,
                info.deviceId,
                info.appId,
                paramsMap,
                info.sslEnabled
            ).toPromise();
        } catch (e) {
            throw new Error('failed to auto personalize with server');
        }
    }
}
