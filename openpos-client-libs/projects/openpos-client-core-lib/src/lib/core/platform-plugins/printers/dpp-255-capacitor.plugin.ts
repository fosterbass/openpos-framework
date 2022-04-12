import { IPrinter } from './printer.interface';
import { IPlatformPlugin } from '../platform-plugin.interface';
import { Observable, from, throwError, of } from 'rxjs';
import { Injectable } from '@angular/core';
import { CapacitorService } from '../../services/capacitor.service';
import { catchError, switchMap, take, timeout } from 'rxjs/operators';
import { registerPlugin } from '@capacitor/core';
import { InfineaPlugin } from '../infinea-plugin.interface';


export const dpp255Capacitor = registerPlugin<InfineaPlugin>('Dpp255Capacitor');
@Injectable({
    providedIn: 'root'
})
export class Dpp255CapacitorPlugin implements IPrinter, IPlatformPlugin {
    id = 'DPP255CAP';
    constructor(private capacitorService: CapacitorService) {
    }

    pluginPresent(): boolean {
        return this.capacitorService.isRunningInCapacitor() && this.capacitorService.isPluginAvailable('Dpp255Capacitor');
    }

    initialize(): Observable<string> {
        return from(dpp255Capacitor.initialize()).pipe(
            take(1),
            timeout(10000),
            switchMap(() => {
                dpp255Capacitor.addListener('status', (e) => {
                    console.log('Infinea printer status: ' + JSON.stringify(e));
                });
                return of('DPP Printer initialized');
            }),
            catchError(e => {
                return throwError(e);
            })
        );
    }

    isSupported(): boolean {
        return this.pluginPresent();
    }

    print(html: string): Observable<void> {
        return from(dpp255Capacitor.print({ data: html })) as Observable<void>;
    }

    name(): string {
        return 'Dpp255Capacitor';
    }

}
