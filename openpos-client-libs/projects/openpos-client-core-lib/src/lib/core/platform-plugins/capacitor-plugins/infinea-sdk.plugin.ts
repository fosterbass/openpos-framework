import { IPlatformPlugin } from '../platform-plugin.interface';
import { from, Observable, of, throwError } from 'rxjs';
import { Injectable } from '@angular/core';
import { ConfigChangedMessage } from '../../messages/config-changed-message';
import { ConfigurationService } from '../../services/configuration.service';
import { CapacitorService } from '../../services/capacitor.service';
import { catchError, switchMap } from 'rxjs/operators';
import { registerPlugin } from '@capacitor/core';
import { InfineaPlugin } from '../infinea-plugin.interface';
import { CapacitorMessage } from '../../messages/capacitor-message';

export const infineaSdk = registerPlugin<InfineaPlugin>('InfineaSdk');
@Injectable({
    providedIn: 'root'
})
export class InfineaSdkPlugin implements IPlatformPlugin {
    id = 'INFINEA_SDK';
    initializeObservable: Observable<any>;

    constructor(private config: ConfigurationService, private capacitorService: CapacitorService) {
        if (this.pluginPresent()) {
            this.config.getConfiguration('InfineaCapacitor').subscribe((message: CapacitorMessage) => {
                if (message.licenseKey) {
                    this.initializeObservable = from(infineaSdk.initialize({
                        apiKey: message.licenseKey
                    }));
                }
            });
        }
    }

    initialize(): Observable<string> {
        return this.initializeObservable.pipe(
            switchMap(() => of('Infinea SDK initialized')),
            catchError(e => {
                return throwError(e);
            })
        );
    }

    name(): string {
        return 'InfineaSdk';
    }

    pluginPresent(): boolean {
        return this.capacitorService.isRunningInCapacitor() && this.capacitorService.isPluginAvailable('InfineaSdk');
    }
}

