import { Injectable } from '@angular/core';
import { Capacitor, registerPlugin } from '@capacitor/core';
import { MessageTypes } from '../../../messages/message-types';

import { SessionService } from '../../../services/session.service';
import { ConfigProvider } from './capacitor-plugin';

@Injectable({providedIn: 'root'})
export class CapacitorPlatformConfigProvider {
    constructor(
        session: SessionService
    ) {
        if (Capacitor.isPluginAvailable('ConfigProvider')) {
            ConfigProvider.getConfig()
                .then(value => {
                    Object.keys(value).forEach(key => {
                        session.sendMessage({
                            type: MessageTypes.CONFIG_CHANGED,
                            configType: key,

                            ...value[key]
                        })
                    });
                });
        }
     }
}
