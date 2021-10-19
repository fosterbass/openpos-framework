import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { MessageTypes } from '../../../messages/message-types';
import { SessionService } from '../../../services/session.service';
import { configProvider } from './capacitor-plugin';

@Injectable({ providedIn: 'root' })
export class CapacitorPlatformConfigProvider {
    constructor(
        session: SessionService
    ) {
        if (Capacitor.isPluginAvailable('ConfigProvider')) {
            configProvider.getConfig()
                .then(value => {
                    Object.keys(value).forEach(key => {
                        session.sendMessage({
                            type: MessageTypes.CONFIG_CHANGED,
                            configType: key,

                            ...value[key]
                        });
                    });
                });
        }
    }
}
