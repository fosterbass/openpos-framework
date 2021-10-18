import { IVersion } from './../interfaces/version.interface';
import { VERSIONS } from './../../version';
import { Injectable } from '@angular/core';
import { SessionService } from './session.service';
import { CONFIGURATION } from './../../configuration/configuration';
import { distinct, filter, map } from 'rxjs/operators';
import { BehaviorSubject, Observable, ReplaySubject } from 'rxjs';
import { ConfigChangedMessage } from '../messages/config-changed-message';
import { ThemeChangedMessage } from '../messages/theme-changed-message';
import { VersionsChangedMessage } from '../messages/versions-changed-message';
import { MessageTypes } from '../messages/message-types';

@Injectable({
    providedIn: 'root',
})
export class ConfigurationService {

    public versions: Array<IVersion> = [];
    public theme$ = new BehaviorSubject<string>('openpos-default-theme');

    private config$ = new ReplaySubject<Map<string, ConfigChangedMessage>>(1);

    constructor(private sessionService: SessionService) {
        const capturedConfig = new Map<string, ConfigChangedMessage>();
        this.sessionService.getMessages(MessageTypes.CONFIG_CHANGED).pipe(
            map(m => m as ConfigChangedMessage),
            filter(m => !!m)
        ).subscribe({
            next: c => {
                capturedConfig.set(c.configType, c);
                this.config$.next(capturedConfig);
            }
        });

        this.getConfiguration('uiConfig').subscribe(m => this.mapConfig(m));
        this.getConfiguration<ThemeChangedMessage>('theme').subscribe(m => this.theme$.next(m.name));
        this.getConfiguration<VersionsChangedMessage>('versions').subscribe(m => {
            this.versions = m.versions.map(v => v);
            this.versions = this.versions.concat(VERSIONS as IVersion[]);
        });
    }

    public getConfiguration<T extends ConfigChangedMessage>(configType: string): Observable<T> {
        return this.config$.pipe(
            map(m => m.get(configType) as T),
            filter(m => m && m.configType === configType),
            distinct()
        );
    }

    protected mapConfig(response: any) {
        for (const p of Object.keys(response)) {
            if (CONFIGURATION.hasOwnProperty(p)) {
                const configPropertyType = typeof CONFIGURATION[p];
                const responsePropertyType = typeof response[p];
                try {
                    if (configPropertyType !== responsePropertyType) {
                        if (configPropertyType === 'string') {
                            CONFIGURATION[p] = response[p].toString();
                        } else {
                            CONFIGURATION[p] = JSON.parse(response[p]);
                        }
                    } else {
                        CONFIGURATION[p] = response[p];
                    }
                } catch (e) {
                    console.warn(`Failed to convert configuration response property '${p}' with value [${response[p]}] ` +
                        `and type '${responsePropertyType}' to Configuration[${p}] of type '${configPropertyType}'` +
                        ` Error: ${e}`);
                }
            }
        }
    }

}
