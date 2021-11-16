import { Injectable } from '@angular/core';
import { Observable, of, Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StorageContainer } from '../storage-container';
import { CordovaService } from '../../../core/services/cordova.service';
import { CordovaPlatform } from '../../platforms/cordova.platform';


declare var NativeStorage: any;
const VALUE_NOT_FOUND = 2;

@Injectable({
    providedIn: 'root'
})
export class CordovaStorageService implements StorageContainer {

    private subscription: Subscription;

    constructor(private cordovaService: CordovaService, private cordovaPlatform: CordovaPlatform) {
    }

    name(): string {
        return 'cordova-storage';
    }

    isAvailable(): Observable<boolean> {
        return this.cordovaService.onDeviceReady.pipe(
            filter(m => m === 'deviceready'),
            map(m => true)
        );
    }

    isSupported(): boolean {
        console.log(`checking cordova storage service, result:  ${this.cordovaService.isRunningInCordova()}`);
        return this.cordovaService.isRunningInCordova();
    }

    initialize(): Observable<string> {
        return of('initialized Cordova storage plugin');
    }

    getValue(key: string): Observable<string | undefined> {
        // use Observable default constructor
        // cleanup subscription / use return.
        return new Observable(observer => {
            // pipe.map
            this.subscription = this.cordovaService.onDeviceReady.subscribe(message => {
                if (message) {
                    NativeStorage.getString(key,
                        (value) => {
                            if (value.code !== undefined) {
                                if (value.code === VALUE_NOT_FOUND) {
                                    console.log(`pumping 'undefined' for ${key}`);
                                    observer.next(undefined);
                                    observer.complete();
                                } else {
                                    console.log(`Raising ERROR ${JSON.stringify(value)}`);
                                    console.log(JSON.stringify(value));
                                    observer.error(value);
                                }
                            } else {
                                console.log(`getValue (successful) ${key}=${value}`);
                                observer.next(value);
                                observer.complete();
                            }
                        },
                        (err) => {
                            console.log(err.message);
                            observer.error(err);
                        });
                }
            });
        });
    }

    setValue(key: string, value: string): Observable<void> {
        return new Observable<void>(observer => {
            NativeStorage.putString(key, value,
                () => console.log('Succeeded in setting shared preference: ' + key + ' ' + value),
                error => console.log('Failed to store value for key ' + key + ' ' + error.message));
            observer.complete();
        });
    }

    clear(): Observable<void> {
        return new Observable<void>(observer => {
            NativeStorage.clear(
                () => {
                    console.info('Native storage cleared successfully');
                    observer.complete();
                },
                error => {
                    console.error(`Storage failed to clear: ${error}`);
                    observer.error(error);
                }
            );
        });
    }

    remove(key: string): Observable<void> {
        return new Observable<void>(observer => {
            NativeStorage.remove(key,
                () => {
                    NativeStorage.getString(key,
                        (value) => {
                            if (value === null || value === undefined) {
                                console.info(`Removed local storage key '${key}' successfully. Value is null/undefined.`);
                                observer.complete();
                            } else if (value.code === undefined) {
                                console.info(`Removed local storage key '${key}' successfully`);
                                observer.complete();
                            } else {
                                NativeStorage.putString(key, null,
                                    () => {
                                        console.info(`Removed local storage key '${key}' successfully. Value set to null.`);
                                        observer.complete();
                                    },
                                    (er) => {
                                        console.error(`Failed to remove local storage key '${key}'. Error: ${er}`);
                                        observer.error();
                                    }
                                );
                            }
                        },
                        (err) => {
                            console.error(`Failed to verify removal of local storage key '${key}'. Error: ${err}`);
                            observer.error(err);
                        });
                },
                (err) => {
                    console.error(`Failed to remove local storage for key '${key}'.  Error: ${JSON.stringify(err)}`);
                    observer.error(err);
                }
            );
        });
    }
}
