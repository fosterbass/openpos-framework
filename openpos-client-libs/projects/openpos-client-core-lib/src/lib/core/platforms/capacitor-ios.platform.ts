import { Injectable } from '@angular/core';
import { Capacitor, Plugins } from '@capacitor/core';
import { Observable, of } from 'rxjs';
import { IPlatformInterface } from './platform.interface';

@Injectable({
    providedIn: 'root'
})
export class CapacitorIosPlatform implements IPlatformInterface {
    getName(): string {
        return 'capacitor-ios';
    }

    platformPresent(): boolean {
        return Capacitor.getPlatform() === 'ios';
    }

    platformReady(): Observable<string> {
        // the capacitor platform doesn't have 'ready' phase, its just ready...
        const plugins = Object.keys(Plugins).join(', ');
        return of('capacitor for iOS loaded', 'capacitor plugins loaded: ' + plugins);
    }
}
