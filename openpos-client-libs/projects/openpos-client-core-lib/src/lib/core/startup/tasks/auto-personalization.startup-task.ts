import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { AutoPersonalizationRequest } from '../../personalization/auto-personalization-request.interface';
import { AutoPersonalizationParametersResponse } from '../../personalization/device-personalization.interface';
import { PersonalizationService } from '../../personalization/personalization.service';
import { StartupTask } from '../startup-task';

export abstract class AutoPersonalizationStartupTask implements StartupTask {
    constructor(protected personalization: PersonalizationService) { }

    abstract execute(): void | Promise<void> | Observable<any>;

    protected async getPersonalizationParameters(
            request: AutoPersonalizationRequest,
            url: string
    ): Promise<AutoPersonalizationParametersResponse> {
        let info: AutoPersonalizationParametersResponse;

        try {
            info = await this.personalization.getAutoPersonalizationParameters(request, url).pipe(
                first()
            ).toPromise();

            return info;
        } catch (e) {
            throw new Error(`Failed to get personalization parameters using url '${url}'. Error: ${JSON.stringify(e)}`);
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
                await this.personalization.personalize({
                    serverConnection: { host: info.serverAddress, port: +info.serverPort, secured: info.sslEnabled },
                    deviceId: info.deviceId,
                    appId: info.deviceId,
                    params: paramsMap,
                    pairedDevice: { deviceId: info.pairedDeviceId, appId: info.pairedAppId }
                }).toPromise();
            }
        } catch (e) {
            throw new Error(`Failed to auto personalize with server. params: ${JSON.stringify(info)}. Error: ${JSON.stringify(e)}`);
        }
    }
}
