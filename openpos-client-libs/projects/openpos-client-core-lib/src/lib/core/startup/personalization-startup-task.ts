import {IStartupTask} from './startup-task.interface';
import {PersonalizationService} from '../personalization/personalization.service';
import {concat, interval, merge, Observable, of, Subject, throwError, defer, iif} from 'rxjs';
import {MatDialog} from '@angular/material';
import {StartupTaskNames} from './startup-task-names';
import {Injectable} from '@angular/core';
import {StartupTaskData} from './startup-task-data';
import {Params} from '@angular/router';
import {PersonalizationComponent} from '../personalization/personalization.component';
import {
    catchError,
    filter,
    map,
    retryWhen,
    switchMap,
    take,
    timeout,
    tap, flatMap
} from 'rxjs/operators';
@Injectable({
    providedIn: 'root',
})
export class PersonalizationStartupTask implements IStartupTask {
    name = StartupTaskNames.PERSONALIZATION;

    order = 500;

    constructor(protected personalization: PersonalizationService, protected matDialog: MatDialog) {}

    execute(data: StartupTaskData): Observable<string> {

        let observableToRun: Observable<string>;

        if (this.hasPersonalizationQueryParams(data.route.queryParams)) {
            observableToRun =  this.doPersonalizeWithRetry(
                'Attempting to personalize using query parameters',
                this.personalizeFromQueueParams(data.route.queryParams)
            );
        } else {
            observableToRun = this.personalization.hasSavedSession().pipe(
                flatMap(hasSavedSession => {
                    if (hasSavedSession) {
                        return this.doPersonalizeWithRetry(
                            'Attempting to personalize from saved token',
                            this.personalization.personalizeFromSavedSession()
                        )
                    } else {
                        return this.promptForPersonalization();
                    }
                })
            );
        }

        return observableToRun;
    }

    private promptForPersonalization(): Observable<string> {
        return concat(
            of("No saved session found, prompting manual personalization"),
            this.matDialog.open(
                PersonalizationComponent, {
                    disableClose: true,
                    hasBackdrop: false,
                    panelClass: 'openpos-default-theme'
                }
            ).afterClosed().pipe(take(1)));
    }

    private doPersonalizeWithRetry(attemptMessage: string, personalize$: Observable<string>): Observable<string> {
        let messages = new Subject<string>();
        return concat(
            of(attemptMessage),
            merge(
                messages,
                personalize$.pipe(
                    retryWhen(errors =>
                        errors.pipe(
                            switchMap(() => interval(1000),
                                (error, time) => `${error} \n Retry in ${5 - time}`),
                            tap(result => messages.next(result)),
                            filter(result => result.endsWith('0')),
                            tap(() => messages.next(attemptMessage))
                        )
                    ),
                    take(1),
                    tap(() => messages.complete())
                ))
        );

    }
    hasPersonalizationQueryParams(queryParams: Params): boolean {
        return queryParams.deviceId && queryParams.appId && queryParams.serverName && queryParams.serverPort;

    }

    personalizeFromQueueParams(queryParams: Params): Observable<string> {
        const deviceId = queryParams.deviceId;
        const appId = queryParams.appId;
        const serverName = queryParams.serverName;
        let serverPort = queryParams.serverPort;
        let sslEnabled = queryParams.sslEnabled;

        const personalizationProperties = new Map<string, string>();
        const keys = Object.keys(queryParams);
        if (keys) {
            for (const key of keys) {
                if (key !== 'deviceId' && key !== 'serverName' && key !== 'serverPort' && key !== 'sslEnabled') {
                    personalizationProperties.set(key, queryParams[key]);
                }
            }
        }

        if (deviceId && serverName) {
            serverPort = !serverPort ? 6140 : serverPort;
            sslEnabled = !sslEnabled ? false : sslEnabled;

            return this.personalization.personalize(serverName, serverPort, deviceId, appId, personalizationProperties, sslEnabled);
        }

        return throwError('Personalizing using Query Params failed');
    }
}
