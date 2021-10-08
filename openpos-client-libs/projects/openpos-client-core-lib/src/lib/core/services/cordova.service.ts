import { Injectable } from '@angular/core';
import {Observable, Subject, BehaviorSubject, Subscription} from "rxjs";
import {filter, map} from "rxjs/operators";


declare var cordova: any;
declare var window: any;

@Injectable({
    providedIn: 'root',
  })
export class CordovaService {
    public onDeviceReady: Subject<string> = new BehaviorSubject<string>(null);
    private _isRunningInCordova: boolean = null;
    public plugins: any;

    private subscription: Subscription;

    constructor() {
        document.addEventListener('deviceready', () => {
                console.info('Cordova devices are ready');

                this._isRunningInCordova = true;
                this.plugins = cordova.plugins;
                this.onDeviceReady.next(`deviceready`);
            },
            false
        );
    }

    public isRunningInCordova(): boolean {

        if (this._isRunningInCordova == null) {   
            this._isRunningInCordova = typeof cordova !== 'undefined' && !this.isRunningInBrowser();
        }
        console.log(`Cordova check is ${typeof cordova !== 'undefined'}, running in browser = ${this.isRunningInBrowser()}. Returning ${this._isRunningInCordova}`);

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
                console.info(`DeviceName requested...`)
                try {
                    console.log(`window['device']['serial']=${window['device']['serial']}`);
                    return window['device']['serial'];
                } catch (e) {
                    console.error(`Error getting device name:`, JSON.stringify(e));
                    return 'UNKNOWN';
                }
            })
        );
    }

}


