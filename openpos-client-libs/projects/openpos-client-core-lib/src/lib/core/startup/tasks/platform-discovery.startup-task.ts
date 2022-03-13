import { Inject, Injectable, Optional } from '@angular/core';
import { EMPTY, merge, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { IPlatformInterface, PLATFORMS } from '../../platforms/platform.interface';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class PlatformDiscoveryStartupTask implements StartupTask {
    static readonly taskName = 'PlatformDiscoveryStartupTask';

    constructor(
        @Optional() @Inject(PLATFORMS) private _platforms: IPlatformInterface[]
    ) { }

    execute(): Observable<void> {
        const notPresentPlatforms = this._platforms.filter(p => !p.platformPresent());

        if (notPresentPlatforms.length > 0) {
            console.debug(`platform(s) [${notPresentPlatforms.map(p => p.getName()).join(', ')}] not present; removing...`);
            notPresentPlatforms.forEach(platform => {
                this._platforms.splice(this._platforms.indexOf(platform), 1);
            });
        }

        if (this._platforms && this._platforms.length > 0) {
            return merge(...this._platforms.map(p => p.platformReady().pipe(
                map(() => { })
            )));
        } else {
            console.debug('no platforms detected');
            return EMPTY;
        }
    }
}
