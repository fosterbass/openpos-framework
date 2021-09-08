import { Injectable } from '@angular/core';

import { Capacitor } from '@capacitor/core';
import { StatusBar } from '@capacitor/status-bar';

import { Observable, from } from 'rxjs';
import { map } from 'rxjs/operators';
import { IPlatformPlugin } from '../platform-plugins/platform-plugin.interface';

@Injectable({
    providedIn: 'root'
})
export class CapacitorStatusBarPlatformPlugin implements IPlatformPlugin {
    name(): string {
        return 'cap-status-bar';
    }
    
    pluginPresent(): boolean {
        return Capacitor.isPluginAvailable('StatusBar');
    }

    initialize(): Observable<string> {
        return from(StatusBar.hide()).pipe(
            map(() => 'capacitor status bar hide requested')
        );
    }
}
