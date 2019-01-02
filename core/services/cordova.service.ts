import { Injectable } from '@angular/core';
import { Subject, BehaviorSubject } from 'rxjs';
import { Logger } from './logger.service';
import { SessionService } from './session.service';

declare var cordova: any;

@Injectable({
    providedIn: 'root',
  })
export class CordovaService {
    public onDeviceReady: Subject<string> = new BehaviorSubject<string>(null);
    private _isRunningInCordova: boolean = null;
    public plugins: any;

    constructor(private log: Logger, public session: SessionService) {
        document.addEventListener('deviceready', () => {
                this.log.info('Cordova devices are ready');
                this._isRunningInCordova = true;
                this.plugins = cordova.plugins;
                this.onDeviceReady.next(`deviceready`);
            },
            false
        );
    }

    public isRunningInCordova(): boolean {
        if (this._isRunningInCordova == null) {
            this._isRunningInCordova = typeof cordova !== 'undefined' && !this.session.isRunningInBrowser();
        }

        return this._isRunningInCordova;
    }

    public get cordova(): any {
        return this.isRunningInCordova() ? cordova : null;
    }

    public isPluginsAvailable() {
        return this.isRunningInCordova() && this.plugins;
    }
}


