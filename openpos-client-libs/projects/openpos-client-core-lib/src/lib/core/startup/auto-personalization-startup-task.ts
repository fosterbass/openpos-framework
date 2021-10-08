import {catchError, first, flatMap, switchMap, take, tap, timeout} from 'rxjs/operators';
import {IStartupTask} from './startup-task.interface';
import {PersonalizationService} from '../personalization/personalization.service';
import {concat, Observable, of, Subject} from 'rxjs';
import {MatDialog} from '@angular/material';
import {Injectable} from '@angular/core';
import {StartupTaskData} from './startup-task-data';
import {Zeroconf, ZeroconfService} from "@ionic-native/zeroconf";
import {StartupTaskNames} from "./startup-task-names";
import {WrapperService} from "../services/wrapper.service";
import {Configuration} from "../../configuration/configuration";


@Injectable({
    providedIn: 'root',
})
export class AutoPersonalizationStartupTask implements IStartupTask {
    name = StartupTaskNames.AUTO_PERSONALIZATION;
    order = 490;
    private readonly TYPE = '_jmc-personalize._tcp.';
    private readonly DOMAIN = 'local.';

    constructor(protected personalization: PersonalizationService, protected matDialog: MatDialog, protected wrapperService: WrapperService) {
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
                        return this.personalizeUsingZeroConf(message).subscribe();
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
                        return this.attemptAutoPersonalize(observer, url, deviceName);
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
            return concat(
                of("Attempting to retrieve personalization params via hostname"),
                this.wrapperService.getDeviceName().pipe(
                    tap(deviceName => name = deviceName),
                    flatMap(() => this.attemptAutoPersonalize(observer, Configuration.autoPersonalizationServicePath, name)),
                    catchError(e => {
                        observer.next()
                        this.logPersonalizationError(e);
                        observer.complete();
                        return of("");
                    })),
            );
        } else {
            observer.complete();
        }
    }

    private attemptAutoPersonalize(observer: Subject<string>, url: string, deviceName: string): Observable<string> {
        return this.personalization.getAutoPersonalizationParameters(deviceName, url)
            .pipe(
                flatMap(info => {
                    if (info) {
                        // Handle case when personalizationParams come through as a Javascript object
                        if (info.personalizationParams && ! (info.personalizationParams instanceof Map)) {
                            info.personalizationParams = new Map<string, string>(Object.entries(info.personalizationParams));
                        }
                        observer.next('Storing personalization parameters received from server.');
                        this.personalization.store(info.serverAddress, info.serverPort, info.deviceId, info.appId, info.personalizationParams, info.sslEnabled);
                        observer.complete();
                        return of("");
                    }
                    observer.complete();
                }),
                catchError(e => {
                    console.log('[AutoPersonalizationTask] error during attempt to Auto personalize');
                    observer.next(`Error during auto-personalization to host '${url}': ${JSON.stringify(e)}`);
                    this.logPersonalizationError(e);
                    throw(e);
                }));
    }

    private logPersonalizationError(error: any): void {
        console.log("[AutoPersonalizationTask] Error during auto-personalization: " + JSON.stringify(error))
    }
}
