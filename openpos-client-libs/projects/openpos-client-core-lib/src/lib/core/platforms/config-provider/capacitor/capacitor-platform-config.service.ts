import { Injectable } from '@angular/core';
import { Plugins as CapacitorPlugins, Capacitor } from '@capacitor/core';
import { MessageTypes } from '../../../messages/message-types';

import { SessionService } from '../../../services/session.service';

@Injectable({providedIn: 'root'})
export class CapacitorPlatformConfigProvider {
    constructor(
        session: SessionService
    ) {
        if (Capacitor.isPluginAvailable('ConfigProvider')) {
            CapacitorPlugins.ConfigProvider.getConfig()
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
