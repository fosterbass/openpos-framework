import { IPlatformPlugin } from '../platform-plugin.interface';
import { Observable, Subject, of } from 'rxjs';
import {filter, take, tap} from 'rxjs/operators';
import { SessionService } from '../../services/session.service';
import {CordovaService} from '../../services/cordova.service';
import { Injectable } from '@angular/core';
import { MessageTypes } from '../../messages/message-types';

@Injectable({
    providedIn: 'root'
})
// Depends on the Cordova plugin cordova-plugin-app-exit@0.0.1
export class ExitAppPlugin implements IPlatformPlugin {

    private initialized = false;
    private exitAppCordovaPlugin;

    constructor( protected cordovaService: CordovaService, protected sessionService: SessionService) { }

    name(): string {
        return 'ExitAppCordovaPlugin';
    }

    pluginPresent(): boolean {
        let present = false;
        if (window.hasOwnProperty('navigator')) {
            // tslint:disable-next-line:no-string-literal
            if (window['navigator'].hasOwnProperty('app')) {
                present = true;
            }
        }
        return present;
    }

    initialize(): Observable<string> {
        if (this.initialized) {
            return of();
        }

        return new Observable<string>( observer => {
            return this.cordovaService.onDeviceReady.pipe(
                filter(m => m === 'deviceready'),
                take(1),
                tap(() => {
                    const objHold: any = window.navigator;
                    this.exitAppCordovaPlugin = window.navigator ? objHold.app : false;
                    if (!this.exitAppCordovaPlugin) {
                        console.error(`Tried to initialize plugin ${this.name()} which is not present`);
                        observer.error(`Tried to initialize plugin ${this.name()} which is not present`);
                    }

                    this.sessionService.getMessages(MessageTypes.EXIT_CLIENT_APP).subscribe(message => this.exitApp());
                    this.initialized = true;
                    console.info('ExitAppCordovaPlugin initialize completed' );
                    observer.next('ExitApp plugin successfully initialized');
                    observer.complete();
                })
            ).subscribe();
        });
    }

    private exitApp() {
        console.info('ExitApp - Exiting the app');
        this.exitAppCordovaPlugin.exitApp();
    }
}
