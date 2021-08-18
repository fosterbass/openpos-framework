import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

import { combineLatest, concat, defer, iif, of } from "rxjs";
import { bufferTime, catchError, filter, groupBy, map, mergeMap, publishReplay, refCount, switchMap, tap } from "rxjs/operators";

import { VERSIONS } from '../../../version';
import { ConfigurationService } from "../../services/configuration.service";
import { ConfigChangedMessage } from "../../messages/config-changed-message";
import { PersonalizationService } from "../../personalization/personalization.service";
import { SessionService } from "../../services/session.service";
import { ConsoleScraper, LogLevel } from "../console-scraper.service";
import { CapacitorService } from "../../services/capacitor.service";

interface NewRelicMessageGroup {
    common?: { [key: string]: any },
    logs: NewRelicLogMessage[],
}

interface NewRelicLogMessage {
    timestamp?: number,
    message: string,
    log_level?: LogLevel,
    
    // attributes
    [key: string]: any,
}

export class NewRelicLoggerConfig extends ConfigChangedMessage {
    enabled: boolean;
    apiKey?: string;

    constructor() {
        super('new-relic-logger');
    }
}

@Injectable({ providedIn: 'root' })
export class NewRelicSink {
    constructor(
        consoleScraper: ConsoleScraper,
        http: HttpClient, 
        config: ConfigurationService, 
        personalization: PersonalizationService,
        sessionService: SessionService,
        capacitorService: CapacitorService
    ) {
        combineLatest(
            config.getConfiguration<NewRelicLoggerConfig>('new-relic-logger').pipe(
                map(v => ({ enabled: v.enabled, apiKey: v.apiKey })),
            ),
            personalization.getAppId$(),
            personalization.getDeviceId$(),
            personalization.getServerName$(),
            personalization.getServerPort$(),
            concat(
                of('none'),
                sessionService.screenMessage$.pipe(map(s => s.screenType)),
            ),
            iif(
                () => capacitorService.isRunningInCapacitor(),
                defer(() => capacitorService.getDeviceName()),
                of(undefined)
            ),
            consoleScraper.messages$.pipe(
                map(cm => <NewRelicLogMessage> {
                    log_level: cm.level,
                    message: cm.message,
                    timestamp: Math.round(Date.now())
                }),
                publishReplay(100, 5000),
                refCount()
            )
        ).pipe(
            map(c => (<NewRelicLogMessage> {
                timestamp: Math.round(Date.now()),
                config: c[0],
                app_id: c[1] || undefined,
                device_id: c[2] || undefined,
                server_name: c[3] || undefined,
                server_port: c[4] || undefined,
                screen: c[5],
                physical_device_name: c[6],
                message: c[7]
            })),
            filter(g => !!g.config && g.config.enabled && !!g.config.apiKey),
            bufferTime(1000),
            filter(logs => logs.length > 0),
            map(logs => ({
                apiKey: logs[0].config.apiKey,
                groups: Array.of(<NewRelicMessageGroup> {
                    logs: logs.map(v => ({
                        ...v,
                        config: undefined,
                    }))
                })
            })),
            mergeMap(request => http.post(
                'https://log-api.newrelic.com/log/v1', 
                request.groups, 
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'Api-Key': request.apiKey
                    }
                }).pipe(
                    catchError(() => of())
                )
            )
        ).subscribe();
    }
}
