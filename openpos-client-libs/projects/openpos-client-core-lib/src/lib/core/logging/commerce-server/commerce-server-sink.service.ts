import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ServerLogEntry } from './server-log-entry';
import { LogMethodType } from './log-method-type.enum';
import { of, Subscription, iif, combineLatest } from 'rxjs';
import { bufferTime, filter, catchError, switchMap, map, mergeMap } from 'rxjs/operators';
import { ConfigurationService } from '../../services/configuration.service';
import { ServerLoggerConfiguration } from './server-logger-configuration';
import { DiscoveryService } from '../../discovery/discovery.service';
import { ConsoleScraper } from '../console-scraper.service';

@Injectable({ providedIn: 'root' })
export class CommerceServerSink implements OnDestroy {
    consoleSubscription: Subscription;

    constructor(
        consoleScraper: ConsoleScraper,
        http: HttpClient,
        discoveryService: DiscoveryService,
        configurationService: ConfigurationService
    ) {
        this.consoleSubscription = combineLatest([
            configurationService.getConfiguration<ServerLoggerConfiguration>('server-logger'),
            discoveryService.getDeviceAppApiServerBaseUrl$(),
        ]).pipe(
            map(settings => ({
                config: settings[0],
                apiUrl: settings[1] + '/clientlogs'
            })),
            switchMap(settings => iif(
                () => settings.config && (settings.config as ServerLoggerConfiguration).enabled === 'true',

                // if true
                consoleScraper.messages$.pipe(
                    map(cm => {
                        let level = LogMethodType.info;

                        switch (cm.level) {
                            case 'debug':
                                level = LogMethodType.debug;
                                break;

                            case 'error':
                                level = LogMethodType.error;
                                break;

                            case 'warn':
                                level = LogMethodType.warn;
                                break;
                        }

                        return new ServerLogEntry(level, Date.now(), cm.message);
                    }),
                    bufferTime(settings.config.logBufferTime || 300),
                    filter(messages => messages.length > 0),
                    mergeMap(messages => http.post(settings.apiUrl, messages).pipe(
                        catchError(() => of())
                    ))
                ),

                // else
                of()
            ))
        ).subscribe();
    }

    ngOnDestroy(): void {
        this.consoleSubscription.unsubscribe();
    }
}
