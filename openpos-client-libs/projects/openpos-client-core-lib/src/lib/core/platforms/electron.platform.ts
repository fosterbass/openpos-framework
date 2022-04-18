import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { IPlatformInterface } from './platform.interface';

import './electron';

@Injectable({
    providedIn: 'root'
})
export class ElectronPlatform implements IPlatformInterface {
    getName(): string {
        return 'electron';
    }

    platformPresent(): boolean {
        return !!window.openposElectron;
    }

    platformReady(): Observable<string> {
        return of();
    }
}
