import { IPlatformPlugin } from '../platform-plugin.interface';
import { Observable, Subject,of } from 'rxjs';
import {filter, take, tap} from "rxjs/operators";
import { SessionService } from '../../services/session.service';
import {CordovaService} from "../../services/cordova.service";
import { Injectable } from '@angular/core';
import { ActionMessage } from '../../messages/action-message';
import { MessageTypes } from '../../messages/message-types';

@Injectable({
    providedIn: 'root'
})
export class ExitAppPlugin implements IPlatformPlugin {

    private initialized = false;
    private ExitAppCordovaPlugin;

    constructor(private sessionService: SessionService, protected cordovaService: CordovaService) { }

    name(): string {
        return 'ExitAppCordovaPlugin';
    }

    pluginPresent(): boolean {
        return !!window.hasOwnProperty('ExitApp');
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
                    this.ExitAppCordovaPlugin = window['ExitApp'];
                    this.sessionService.getMessages(MessageTypes.EXIT_CLIENT_APP).subscribe(message => this.exitApp());
                    this.initialized = true;
                    observer.next('ExitApp plugin successfully initialized');
                    observer.complete();
                })
            ).subscribe();
        });
    }

    private exitApp() {
        console.info('ExitApp - Exiting the app');
        this.ExitAppCordovaPlugin.exitApp();
    }
}
