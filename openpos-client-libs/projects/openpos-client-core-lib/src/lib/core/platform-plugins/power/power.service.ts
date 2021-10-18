import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

import { PowerStatus, PowerSupplier, POWER_SUPPLIER_TOKEN } from './power-supplier';

@Injectable({ providedIn: 'root' })
export class Power {
    constructor(
        @Inject(POWER_SUPPLIER_TOKEN) private suppliers: PowerSupplier[]
    ) {
        console.log('initializing power', suppliers.length);
    }

    observePowerStatus(): Observable<PowerStatus> {
        const suppliers = this.suppliers.filter(f => !!f && f.isAvailable);

        if (suppliers.length > 0) {
            console.log(`no power supplier initialized, lets do it`);
            return this.suppliers[0].observePowerStatus();
        }

        console.log(`defaulting power to always 'plugged-in'`);
        return of<PowerStatus>('plugged-in');
    }
}
