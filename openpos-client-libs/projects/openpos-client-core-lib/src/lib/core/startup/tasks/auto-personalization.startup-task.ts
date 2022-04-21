import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { AutoPersonalizationParametersResponse } from '../../personalization/device-personalization.interface';
import { PersonalizationService } from '../../personalization/personalization.service';
import { StartupTask } from '../startup-task';

export abstract class AutoPersonalizationStartupTask implements StartupTask {
    constructor(protected personalization: PersonalizationService) { }

    abstract execute(): void | Promise<void> | Observable<any>;

    protected async getPersonalizationParameters(deviceName: string, url: string): Promise<AutoPersonalizationParametersResponse> {
        let info: AutoPersonalizationParametersResponse;

        try {
            info = await this.personalization.getAutoPersonalizationParameters(
                deviceName,
                url
            ).pipe(
                first()
            ).toPromise();

            return info;
        } catch (e) {
            throw new Error('failed to get personalization parameters');
        }
    }

    protected async personalize(info: AutoPersonalizationParametersResponse): Promise<void> {
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
            if (!!info.deviceToken) {
                await this.personalization.personalizeWithToken(
                    info.serverAddress,
                    info.serverPort,
                    info.deviceToken,
                    info.sslEnabled,
                    info.pairedAppId,
                    info.pairedDeviceId
                ).toPromise();
            } else {
                await this.personalization.personalize(
                    info.serverAddress,
                    info.serverPort,
                    info.deviceId,
                    info.appId,
                    paramsMap,
                    info.sslEnabled,
                    info.pairedAppId,
                    info.pairedDeviceId
                ).toPromise();
            }
        } catch (e) {
            throw new Error('failed to auto personalize with server');
        }
    }
}
