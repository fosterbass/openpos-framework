import { Injectable } from '@angular/core';
import { timer } from 'rxjs';
import { take } from 'rxjs/operators';
import { PersonalizationService } from '../../personalization/personalization.service';
import { SplashScreen } from '../../services/splash-screen.service';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class SavedSessionPersonalizationStartupTask implements StartupTask {
    static readonly taskName = 'SavedSessionPersonalizationStartupTask';

    constructor(
        private _personalization: PersonalizationService,
        private _splashScreen: SplashScreen
    ) { }

    async execute(): Promise<void> {
        if (this._personalization.hasSavedSession()) {
            while (true) {
                try {
                    await this._personalization.personalizeFromSavedSession().toPromise();
                    break;
                } catch (e) {
                    console.warn(`personalization from saved session failed due to reason: ${JSON.stringify(e)}; Retrying...`);

                    let count = 5;

                    while (count > 0) {
                        const isPlural = count > 1;
                        const splashRef = this._splashScreen.pushMessage(`Server personalization failed; Retrying in ${count} ${isPlural ? 'seconds' : 'second'}...`);

                        try {
                            await timer(1000).pipe(take(1)).toPromise();
                        } finally {
                            splashRef.pop();
                        }

                        count--;
                    }

                }
            }

            return;
        }

        throw new Error('no saved session');
    }
}
