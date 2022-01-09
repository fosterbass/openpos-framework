import { Injectable } from '@angular/core';
import { CapacitorService } from './capacitor.service';
import { CordovaService } from './cordova.service';
import { Observable, of } from 'rxjs';
import { ElectronService } from 'ngx-electron';

@Injectable({
    providedIn: 'root'
})
export class AppPlatformService {
    constructor(private capacitorService: CapacitorService, private cordovaService: CordovaService, private electron: ElectronService) {
    }

    public getDeviceName(): Observable<string> {
        if (this.capacitorService.isRunningInCapacitor()) {
            return this.capacitorService.getDeviceName();
        } else if (this.cordovaService.isRunningInCordova()) {
            return this.cordovaService.getDeviceName();
        } else if (this.electron.isElectronApp) {
            return new Observable(observer => {
                const hostname = this.electron.ipcRenderer.sendSync('get-hostname');

                if (hostname) {
                    observer.next(hostname);
                    observer.complete();
                } else {
                    observer.error('failed to retrieve hostname for this device');
                }
            });
        }

        return of(null);
    }
}
