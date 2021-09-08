import {Injectable} from '@angular/core';
import {Capacitor} from "@capacitor/core";
import {Device, DeviceInfo} from '@capacitor/device';
import {from, Observable} from "rxjs";
import {map} from "rxjs/operators";

@Injectable({
    providedIn: 'root',
})
export class CapacitorService {

    public isRunningInCapacitor(): boolean {
        return Capacitor.isNative;
    }

    public isPluginAvailable(plugin: string): boolean {
        return Capacitor.isPluginAvailable(plugin);
    }

    public getDeviceName(): Observable<string> {
        return from(Device.getInfo()).pipe(
            map((info: DeviceInfo) => info.name)
        );
    }
}


