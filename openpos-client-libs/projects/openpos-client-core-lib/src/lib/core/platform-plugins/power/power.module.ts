import { NgModule } from '@angular/core';
import { CapacitorPowerSupplier } from './capacitor/capacitor-power-supplier';
import { POWER_SUPPLIER_TOKEN } from './power-supplier';

@NgModule({
    providers: [
        { provide: POWER_SUPPLIER_TOKEN, useExisting: CapacitorPowerSupplier, multi: true },
    ]
})
export class PowerModule { }
