import { Injectable } from "@angular/core";
import { Capacitor } from "@capacitor/core";
import { Observable, of } from "rxjs";
import { PowerStatus, PowerSupplier } from "../power-supplier";

declare module '@capacitor/core' {
    interface PluginRegistry {
        Power: PowerPlugin;
    }
}

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

            Capacitor.Plugins.Power.subscribe().then(result => {
                observer.next(result.state);
            });

            Capacitor.Plugins.Power.addListener('batteryStatusChanged', details => {
                observer.next(details.state);
            });

            return () => {
                Capacitor.Plugins.Power.unsubscribe();
            };
        });
    }
}
