import {catchError, first, flatMap, map, switchMap, takeUntil, tap, timeout} from 'rxjs/operators';
import {IStartupTask} from './startup-task.interface';
import {PersonalizationService} from '../personalization/personalization.service';
import {concat, defer, EMPTY, Observable, of, Subject, throwError} from 'rxjs';
import {MatDialog} from '@angular/material';
import {Injectable} from '@angular/core';
import {StartupTaskData} from './startup-task-data';
import {Zeroconf, ZeroconfService} from "@ionic-native/zeroconf";
import {StartupTaskNames} from "./startup-task-names";
import {WrapperService} from "../services/wrapper.service";
import {Configuration} from "../../configuration/configuration";
import {EnterpriseConfigService} from "../platform-plugins/enterprise-config/enterprise-config.service";
import {AutoPersonalizationRequest} from "../personalization/auto-personalization-request.interface";


@Injectable({
    providedIn: 'root',
})
export class AutoPersonalizationStartupTask implements IStartupTask {
    name = StartupTaskNames.AUTO_PERSONALIZATION;
    order = 490;
    private readonly TYPE = '_jmc-personalize._tcp.';
    private readonly DOMAIN = 'local.';

    constructor(protected personalization: PersonalizationService, protected matDialog: MatDialog, protected wrapperService: WrapperService,
                protected enterpriseConfigService: EnterpriseConfigService) {
    }

    execute(data: StartupTaskData): Observable<string> {

        return Observable.create((message: Subject<string>) => {
            message.next('started');
            if (this.personalization.shouldAutoPersonalize()) {
                this.personalization.hasSavedSession().subscribe(hasSavedSession => {
                    if (hasSavedSession) {
                        console.log('[AutoPersonalizationTask] saved session exists')
                        // Let personalizationTask take it from here
                        message.next("Saved personalization session exists, letting default Personalization run")
                        message.complete();
                    } else {
                        console.log('[AutoPersonalizationTask] saved session not found, trying personalization with Zero conf')
                        console.log(`[AutoPersonalizationTask] config: ${JSON.stringify(this.enterpriseConfigService.getConfiguration())}`)
                        return this.personalizeUsingEnterpriseConfig(message).subscribe();
                    }
                })
            } else {
                message.next("No auto personalization available for device");
                message.complete();
            }
        });
    }

/*
    // Switch to this method if you need to test auto personalization
    // locally in your browser
    personalizeUsingZeroConfMock(): Observable<string> {
        let name: string = null;
        return of("jason_device").pipe(
            tap(deviceName => name = deviceName),
            flatMap(() => {
                const url = `localhost:6140/rest/admin/personalizeMe`;
                return this.attemptAutoPersonalize(url, name);
            })
        );
    }
*/

    personalizeUsingEnterpriseConfig(observer: Subject<string>): Observable<string> {
        return defer( () => {
            const config = this.enterpriseConfigService.getConfiguration();
            if (config && config.hasOwnProperty('jmc_autoPersonalizeUrl')) {
                const url = config['jmc_autoPersonalizeUrl'];
                console.info(`[AutoPersonalizationTask] Attempting auto-personalization using URL from Enterprise Configuration: ${url}`);
                observer.next(`Attempting auto-personalization using URL from Enterprise Configuration`);
                return this.wrapperService.getDeviceName().pipe(
                    switchMap(deviceName => {
                        observer.next(`Device name is: ${deviceName}`);
                        return this.attemptAutoPersonalize(observer, url, {deviceName: deviceName, additionalAttributes: config}).pipe(
                            catchError(e => {
                                console.log(`[AutoPersonalizationTask] error during auto-personalize using URL from Enterprise Configuration: ${url}`);
                                this.logPersonalizationError(e);
                                return this.personalizeUsingZeroConf(observer);
                            })
                        );
                    })
                );
            } else {
                observer.next(`No Enterprise Configuration found, falling back to auto-personalization with Zero conf`);
                return this.personalizeUsingZeroConf(observer);
            }
        });

    }

    personalizeUsingZeroConf(observer: Subject<string>): Observable<string> {
        let serviceConfig: ZeroconfService = null;

        return this.wrapperService.getDeviceName().pipe(
             switchMap(deviceName => {
                console.log('[AutoPersonalizationTask] Starting ZeroConf watch on device ', deviceName);
                return Zeroconf.watch(this.TYPE, this.DOMAIN).pipe(
                    timeout(Configuration.autoPersonalizationRequestTimeoutMillis),
                    first(conf => conf.action === 'resolved'),
                    tap(conf => {
                        serviceConfig = conf.service;
                        console.log('[AutoPersonalizationTask] service resolved');
                    }),
                    flatMap(() => {
                        const url = `${serviceConfig.hostname}:${serviceConfig.port}/${serviceConfig.txtRecord.path}`;
                        return this.attemptAutoPersonalize(observer, url, {deviceName: deviceName});
                    }),
                    catchError(e => {
                        console.log('[AutoPersonalizationTask] error during Zeroconf.watch');
                        this.logPersonalizationError(e);
                        return this.personalizeWithHostname(observer);
                    })
                );
            })
        );

    }

    personalizeWithHostname(observer: Subject<string>): Observable<string> {
        const servicePath = Configuration.autoPersonalizationServicePath;
        if (!!servicePath) {
            let name: string = null;
            // @ts-ignore
            return concat(
                of("Attempting to retrieve personalization params via hostname"),
                this.wrapperService.getDeviceName().pipe(
                    tap(deviceName => name = deviceName),
                    flatMap(() => this.attemptAutoPersonalize(observer, Configuration.autoPersonalizationServicePath, {deviceName: name})),
                    // This is causing a ts-lint error, but I cannot figure out why
                    catchError(e => {
                        this.logPersonalizationError(e);
                        observer.complete();
                        return of("");
                    })),
            );
        } else {
            observer.complete();
        }
    }

    private attemptAutoPersonalize(observer: Subject<string>, url: string, request: AutoPersonalizationRequest): Observable<string> {
            // @ts-ignore
        return this.personalization.getAutoPersonalizationParameters(request, url).pipe(
            flatMap(info => {
                if (info) {
                    const stopSignal$ = new Subject();
                    // Handle case when personalizationParams come through as a Javascript object
                    if (info.personalizationParams && ! (info.personalizationParams instanceof Map)) {
                        info.personalizationParams = new Map<string, string>(Object.entries(info.personalizationParams));
                    }
                    observer.next('Personalizing with parameters received from server...');
                    return this.personalization.personalize(
                        info.serverAddress,
                        info.serverPort,
                        info.deviceId,
                        info.appId,
                        info.personalizationParams,
                        info.sslEnabled).pipe(
                            map(msg => {
                                observer.next(msg);
                                observer.complete();
                                stopSignal$.next();
                            }),
                            takeUntil(stopSignal$),
                            catchError(e => {
                                this.logPersonalizationError(e);
                                return throwError(e);
                            })
                        );
                } else {
                    return EMPTY;
                }
            }),
            // This is causing a ts-lint error, but I cannot figure out why
            catchError(e => {
                console.log('[AutoPersonalizationTask] error during attempt to Auto personalize');
                observer.next(`Error during auto-personalization to host '${url}': ${JSON.stringify(e)}`);
                this.logPersonalizationError(e);
                return throwError(e);
            })
        );
    }

    private logPersonalizationError(error: any): void {
        console.log("[AutoPersonalizationTask] Error during auto-personalization: " + JSON.stringify(error))
    }
}
