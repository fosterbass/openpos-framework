import {Injectable} from "@angular/core";
import {IPlatformPlugin} from "../platform-plugin.interface";
import {EMPTY, Observable, of} from "rxjs";
import {filter, take, tap} from "rxjs/operators";
import {CordovaService} from "../../services/cordova.service";
import {IEnterpriseConfig} from "../enterprise-config/enterprise-config.interface";

declare var window: any;

@Injectable({
    providedIn: 'root'
})
export class AirwatchCordovaPlugin implements IPlatformPlugin, IEnterpriseConfig {
    jmcAirwatchPlugin: any;
    private initialized = false;

    constructor(protected cordovaService: CordovaService) {
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
                    this.jmcAirwatchPlugin = window['JMCAirwatchPlugin']
                    this.jmcAirwatchPlugin.setSDKEventCallback((eventName, eventData) => {
                        if (eventName === 'initSuccess') {
                            this.initialized = true;
                            observer.next('Airwatch SDK successfully initialized');
                            observer.complete();
                        } else if (eventName == 'initFailure') {
                            observer.error(`Airwatch SDK failed to initialize. Reason: ${eventData ? (eventData.error ? eventData.error : JSON.stringify(eventData)) : '?'}`);
                        }
                    })
                })
            ).subscribe();
        });
    }

    name(): string {
        return "AirwatchCordovaPlugin";
    }

    configPresent(): boolean {
        return this.pluginPresent();
    }

    pluginPresent(): boolean {
        return !! window.hasOwnProperty('JMCAirwatchPlugin');
    }

    private isInitialized(): boolean {
        if (! this.initialized) {
            console.error('AirwatchCordovaPlugin is not yet initialized, cannot complete requested operation');
            return false;
        }
        return true;
    }

    getConfiguration(): Observable<any> {
        return new Observable<any>( observer => {
            if (! this.isInitialized()) {
                observer.error(`Configuration not available since AirwatchCordovaPlugin is not yet initialized.`);
            } else {
                this.jmcAirwatchPlugin.getCustomConfig(
                    config => {
                        observer.next(config);
                        observer.complete();
                    },
                    error => observer.error(error)
                );
            }
        });
    }

    getString(configName: string): Observable<string> {
        return ! this.isInitialized() ? EMPTY :
            new Observable<string>(observer => {
                this.jmcAirwatchPlugin.getCustomConfigValue(configName,
                    value => {
                        observer.next(value);
                        observer.complete();
                    },
                    error => observer.error(error)
                );
            });
    }

}