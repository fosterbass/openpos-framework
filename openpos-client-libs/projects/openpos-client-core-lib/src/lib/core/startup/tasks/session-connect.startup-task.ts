import { Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { throwError } from 'rxjs';
import { take, timeoutWith } from 'rxjs/operators';
import { CONFIGURATION } from '../../../configuration/configuration';
import { MessageTypes } from '../../messages/message-types';
import { SessionService } from '../../services/session.service';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class SessionConnectStartupTask implements StartupTask {
    constructor(
        private _session: SessionService,
        private _activeRoute: ActivatedRoute
    ) {
        console.log('ctor');
    }

    async execute(): Promise<void> {
        console.log('running session connect task');

        if (!this._session.connected()) {
            const routeSnapshot = this._activeRoute.snapshot;

            for (const key of routeSnapshot.queryParamMap.keys) {
                this._session.addQueryParam(key, routeSnapshot.queryParamMap.get(key));
            }

            this._session.unsubscribe();

            console.log('subscribing to server');
            await this._session.subscribe();

            const startup = this._session.getMessages(MessageTypes.STARTUP).pipe(
                timeoutWith(CONFIGURATION.confirmConnectionTimeoutMillis, throwError(new Error('timed out waiting for server'))),
                take(1)
            );

            console.debug('waiting for startup message from server');
            await startup.toPromise();

            console.info('received startup message from server; start session');
        } else {
            console.error('session already active; reloading...');
            window.location.reload();
            return;
        }
    }
}
