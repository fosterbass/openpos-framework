import { IPlatformPlugin } from '../platform-plugin.interface';
import { Observable, Subject,of } from 'rxjs';
import {filter, take, tap} from "rxjs/operators";
import { SessionService } from '../../services/session.service';
import {CordovaService} from "../../services/cordova.service";
import { Injectable } from '@angular/core';
import { MessageTypes } from '../../messages/message-types';

@Injectable({
    providedIn: 'root'
})
export class ExitAppPlugin implements IPlatformPlugin {

    private initialized = false;
    private ExitAppCordovaPlugin;

    constructor( protected cordovaService: CordovaService, protected sessionService: SessionService) { }

    name(): string {
        return 'ExitAppCordovaPlugin';
    }

    pluginPresent(): boolean {
        let present = false;
        if (window.hasOwnProperty('cordova')) {
            if (window['cordova'].hasOwnProperty('plugins')) {
                present = window['cordova'].plugins.hasOwnProperty('exit');
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
                    this.ExitAppCordovaPlugin = window['cordova'].plugins['exit'];
                    if (!this.ExitAppCordovaPlugin) {
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
        this.ExitAppCordovaPlugin();
    }
}
