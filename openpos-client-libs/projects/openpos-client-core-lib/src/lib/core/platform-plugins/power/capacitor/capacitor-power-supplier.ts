import { Injectable } from '@angular/core';
import { Capacitor, registerPlugin } from '@capacitor/core';
import { Observable } from 'rxjs';
import { PowerStatus, PowerSupplier } from '../power-supplier';

const power = registerPlugin<PowerPlugin>('Power');


interface PowerPlugin {
    subscribe(): Promise<PowerDetails>;
    unsubscribe(): Promise<void>;

    addListener(event: 'batteryStatusChanged', callback: (details: PowerDetails) => void);
}

interface PowerDetails {
    state: 'unplugged' | 'plugged-in';
}

@Injectable({ providedIn: 'root' })
export class CapacitorPowerSupplier implements PowerSupplier {
    get name(): string {
        return 'cap-power';
    }

    get isAvailable(): boolean {
        return Capacitor.isPluginAvailable('Power');
    }

    observePowerStatus(): Observable<PowerStatus> {
        return new Observable(observer => {
            if (!Capacitor.isPluginAvailable('Power')) {
                observer.error('power details not supported on this platform');
                return;
            }

            power.subscribe().then(result => {
                observer.next(result.state);
            });

            power.addListener('batteryStatusChanged', details => {
                observer.next(details.state);
            });

            return () => {
                power.unsubscribe();
            };
        });
    }
}
