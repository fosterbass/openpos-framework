import { Injectable } from '@angular/core';
import { IPlatformPlugin } from '../../../platform-plugin.interface';
import { ScanData, Scanner, ScanOptions } from '../../scanner';
import { from, Observable, of, throwError } from 'rxjs';
import { InfineaBarcodeUtils } from '../infinea-to-openpos-barcode-type';
import { CapacitorService } from '../../../../services/capacitor.service';
import { catchError, switchMap, take, timeout } from 'rxjs/operators';
import { InfineaPlugin } from '../../../infinea-plugin.interface';
import { registerPlugin } from '@capacitor/core';

export const infineaCapacitor = registerPlugin<InfineaPlugin>('InfineaScannerCapacitor');
@Injectable({
    providedIn: 'root'
})
export class InfineaScannerCapacitorPlugin implements IPlatformPlugin, Scanner {

    private _initializedWithError = false;

    constructor(private capacitorService: CapacitorService) {
    }

    name(): string {
        return 'InfineaScannerCapacitor';
    }

    pluginPresent(): boolean {
        return this.capacitorService.isRunningInCapacitor() && this.capacitorService.isPluginAvailable('InfineaScannerCapacitor');
    }

    beginScanning(options: ScanOptions): Observable<ScanData> {
        if (!this.pluginPresent()) {
            return throwError('the infinea scanner plugin is not available');
        }

        if (this._initializedWithError) {
            return of();
        }

        return new Observable(observer => {
            const handle = infineaCapacitor.addListener('scan', (e) => {
                observer.next({
                    type: InfineaBarcodeUtils.convertToOpenposType(e.type),
                    data: e.barcode
                });
            });

            return () => {
                handle.remove();
            };
        });
    }

    initialize(): Observable<string> {

        return from(infineaCapacitor.initialize()).pipe(
            take(1),
            timeout(10000),
            switchMap(() => {
                infineaCapacitor.addListener('status', (e) => {
                    console.log('Infinea Scanner status: ' + JSON.stringify(e));
                });
                return of('Infinea Scanner initialized');
            }),
            catchError(e => {
                this._initializedWithError = true;
                return throwError(e);
            })
        );
    }
}
