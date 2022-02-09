import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { Device, DeviceInfo } from '@capacitor/device';
import { BehaviorSubject, from, Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
})
export class CapacitorService {
    public onDeviceReady: Subject<string> = new BehaviorSubject<string>(null);

    constructor() {
        document.addEventListener('deviceready', () => {
                console.info('Capacitor devices are ready');
                this.onDeviceReady.next(`deviceready`);
                document.addEventListener('backbutton', this.onBackKeyDown, false);
            },
            false
        );
    }

    public isRunningInCapacitor(): boolean {
        return Capacitor.isNativePlatform();
    }

    public isPluginAvailable(plugin: string): boolean {
        return Capacitor.isPluginAvailable(plugin);
    }

    public getDeviceName(): Observable<string> {
        return from(Device.getInfo()).pipe(
            map((info: DeviceInfo) => info.name)
        );
    }

    public onBackKeyDown() {
        console.info('Back button press ignored');
    }
}
