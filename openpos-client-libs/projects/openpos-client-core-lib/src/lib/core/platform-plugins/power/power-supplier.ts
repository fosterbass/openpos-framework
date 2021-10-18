import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';

export const POWER_SUPPLIER_TOKEN = new InjectionToken<PowerSupplier>('POWER_SUPPLIER');

export type PowerStatus = 'unplugged' | 'plugged-in';

export interface PowerSupplier {
    readonly name: string;
    readonly isAvailable: boolean;

    observePowerStatus(): Observable<PowerStatus>;
}
