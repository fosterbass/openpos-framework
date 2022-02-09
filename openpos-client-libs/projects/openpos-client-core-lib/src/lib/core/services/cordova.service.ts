import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { filter, map } from 'rxjs/operators';

declare var cordova: any;
declare var window: any;

@Injectable({
    providedIn: 'root',
  })
export class CordovaService {
    public onDeviceReady: Subject<string> = new BehaviorSubject<string>(null);
    private _isRunningInCordova: boolean = null;
    public plugins: any;

    constructor() {
        document.addEventListener('deviceready', () => {
                console.info('Cordova devices are ready');
                this._isRunningInCordova = true;
                this.plugins = cordova.plugins;
                this.onDeviceReady.next(`deviceready`);
                document.addEventListener('backbutton', this.onBackKeyDown, false);
            },
            false
        );
    }

    public onBackKeyDown() {
        console.info('Back button press ignored');
    }

    public isRunningInCordova(): boolean {
        if (this._isRunningInCordova == null) {
            this._isRunningInCordova = typeof cordova !== 'undefined' && !this.isRunningInBrowser();
        }

        return this._isRunningInCordova;
    }

    public get cordova(): any {
        return this.isRunningInCordova() ? cordova : null;
    }

    public isPluginsAvailable() {
        return this.isRunningInCordova() && this.plugins;
    }

    public isRunningInBrowser(): boolean {
        const app = document.URL.indexOf('http://') === -1 && document.URL.indexOf('https://') === -1;
        return !app;
    }

    public getDeviceName(): Observable<string> {
        return this.onDeviceReady.pipe(
            filter(m => m === 'deviceready'),
            map(m => {
                try {
                    console.log(`window['device']['serial']=${window.device.serial}`);
                    return window.device.serial;
                } catch (e) {
                    console.error(`Error getting device name:`, JSON.stringify(e));
                    return '?';
                }
            })
        );
    }
}


