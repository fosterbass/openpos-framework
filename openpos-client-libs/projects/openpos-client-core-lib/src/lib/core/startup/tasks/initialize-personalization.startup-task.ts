import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';
import { filter, take, timeoutWith } from 'rxjs/operators';
import { PersonalizationService } from '../../personalization/personalization.service';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class InitializePersonalizationStartupTask implements StartupTask {
    name = 'InitializePersonalizationStartupTask';

    constructor(private _personalization: PersonalizationService) { }

    async execute(): Promise<void> {
        await this._personalization.personalizationInitialized$.pipe(
            filter(v => v),
            take(1),
            timeoutWith(10000, throwError(() => new Error('timed out waiting for personalization service to initialize')))
        ).toPromise();
    }
}
