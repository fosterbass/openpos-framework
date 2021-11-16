import { Injectable } from '@angular/core';
import { IStartupTask } from './startup-task.interface';
import { Observable, throwError, concat, defer } from 'rxjs';
import { SessionService } from '../services/session.service';
import { Router } from '@angular/router';
import { StartupTaskData } from './startup-task-data';
import { StartupTaskNames } from './startup-task-names';
import { map, take, timeoutWith } from 'rxjs/operators';
import { CONFIGURATION } from '../../configuration/configuration';
import { MessageTypes } from '../messages/message-types';
import { OpenposMessage } from '../messages/message';

@Injectable()
export class SubscribeToSessionTask implements IStartupTask {
    name = StartupTaskNames.SUBSCRIBE_TO_SESSION;
    order = 600;

    startupMessage$: Observable<OpenposMessage>;

    constructor(
        protected session: SessionService,
        protected router: Router
    ) {
        this.startupMessage$ = this.session.getMessages(MessageTypes.STARTUP);
    }

    execute(data: StartupTaskData): Observable<string> {
        return new Observable(messages => {
            messages.next('started');
            if (!this.session.connected()) {
                console.info(`[SubscribeToSessionTask] session not connected`);
                const subscribe = defer(async () => {
                    console.info(`[SubscribeToSessionTask] Running unsubscribe`);
                    data.route.queryParamMap.keys.forEach(key => {
                        this.session.addQueryParam(key, data.route.queryParamMap.get(key));
                    });
                    this.session.unsubscribe();
                    console.info(`[SubscribeToSessionTask] Waiting for session subscribe ...`);
                    messages.next('Subscribing to server ...');
                    await this.session.subscribe();
                });

                const waitForStartup = this.startupMessage$.pipe(
                    timeoutWith(CONFIGURATION.confirmConnectionTimeoutMillis, throwError('Timed out waiting for server')),
                    map(() => {
                        messages.next('Startup message received');
                        messages.complete();
                    }),
                    take(1)
                );

                return concat(
                    subscribe,
                    new Observable(obs => {
                        messages.next('Waiting for startup message from server...');
                        obs.complete();
                    }),
                    waitForStartup
                ).subscribe();
            } else {
                messages.error('Already subscribed, reloading app');
                // we shouldn't be coming here if we are already subscribed.  lets do a refresh to get a clean start
                window.location.reload();
            }
        });
    }
}
