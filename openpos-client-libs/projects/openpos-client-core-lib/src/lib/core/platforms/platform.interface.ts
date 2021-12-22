import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';

export const PLATFORMS = new InjectionToken<IPlatformInterface[]>('PLATFORMS');

export interface IPlatformInterface {
    getName(): string;
    platformPresent(): boolean;
    platformReady(): Observable<string>;
}
