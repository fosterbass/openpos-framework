import { Injectable, NgZone } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map, mergeMap, switchMap, take, timeout } from 'rxjs/operators';
import { CapacitorMessage } from '../../../messages/capacitor-message';
import { ConfigurationService } from '../../../services/configuration.service';
import { IPlatformPlugin } from '../../platform-plugin.interface';
import { ImageScanner, ScannerViewRef, ScanData } from '../scanner';
import { scandit } from './scandit-plugin.capacitor';

@Injectable({
    providedIn: 'root'
})
export class ScanditCapacitorImageScanner implements ImageScanner, IPlatformPlugin {
    private _initializedWithError = false;

    constructor(private _config: ConfigurationService, private _ngZone: NgZone) { }

    name(): string {
        return 'scandit-cap';
    }

    pluginPresent(): boolean {
        return Capacitor.isPluginAvailable('ScanditNative');
    }

    initialize(): Observable<string> {
        return this._config.getConfiguration('ScanditCapacitor').pipe(
            take(1),
            timeout(10000),
            switchMap((config: CapacitorMessage) => {
                if (config.licenseKey) {
                    let licenseKeyDebug = '<UNDEFINED>';

                    // just some simple safe guards to prevent too much of the key
                    // being easily scraped from the console log. Its not terribly
                    // sensitive, but we should try and hide it at least a little.
                    if (config.licenseKey.length >= 30) {
                        licenseKeyDebug = config.licenseKey.substring(0, 5)
                            + '...'
                            + config.licenseKey.substring(config.licenseKey.length - 5);
                    } else if (config.licenseKey.length >= 15) {
                        licenseKeyDebug = config.licenseKey.substring(0, 3) + '...';
                    } else {
                        licenseKeyDebug = '<SECRET>';
                    }

                    console.debug('initializing capacitor Scandit plugin', { key: licenseKeyDebug });

                    return of(scandit.initialize({
                        apiKey: config.licenseKey
                    }));
                }

                return throwError('could not find Scandit license key');
            }),
            map(() => 'initialized Scandit for Capacitor'),
            catchError(() => {
                this._initializedWithError = true;
                return of('failed to start scandit; will disable');
            })
        );
    }

    beginScanning(view: ScannerViewRef): Observable<ScanData> {
        if (!Capacitor.isPluginAvailable('ScanditNative')) {
            return throwError('the scandit plugin is not available');
        }

        if (this._initializedWithError) {
            return of();
        }

        return new Observable(observer => {
            scandit.addView();

            const updateViewSub = view.viewChanges().pipe(
                mergeMap(d => scandit.updateView(d))
            ).subscribe();

            const handle = scandit.addListener('scan', (e) => {
                this._ngZone.run(() => {
                    observer.next({
                        type: e.symbology,
                        data: e.data
                    });
                });
            });

            return () => {
                handle.remove();
                updateViewSub.unsubscribe();
                scandit.removeView();
            };
        });
    }
}
