import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';
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
        return of();
    }
}
