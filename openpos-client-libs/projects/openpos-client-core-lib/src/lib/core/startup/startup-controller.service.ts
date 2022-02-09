import { Injectable, Injector } from '@angular/core';
import { startupSequence } from './startup-sequence';

export type StartupControllerState = boolean | 'in-progress' | 'error';

export interface StartupControllerStatus {
    readonly completed: StartupControllerState;
    readonly error?: any;
}

@Injectable({
    providedIn: 'root'
})
export class StartupController {
    get status(): StartupControllerStatus {
        return this._status;
    }

    private _status: StartupControllerStatus = {
        completed: false
    };

    constructor(private _injector: Injector) { }

    async beginStartupSequence(): Promise<StartupControllerStatus> {
        if (this._status.completed !== false) {
            throw new Error('startup sequence already initiated');
        }

        this._status = { completed: 'in-progress' };

        try {
            await startupSequence.executeTask(this._injector);
        } catch (e) {
            this._status = { completed: 'error', error: e };
            return;
        }

        this._status = { completed: true };

        return this._status;
    }
}
