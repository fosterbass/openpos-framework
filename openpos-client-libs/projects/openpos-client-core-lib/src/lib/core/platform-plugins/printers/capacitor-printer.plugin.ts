import { Injectable } from '@angular/core';
import { Capacitor, registerPlugin } from '@capacitor/core';
import { from, Observable } from 'rxjs';

import { IPrinter } from './printer.interface';

const print = registerPlugin<PrintPlugin>('Print');

interface PrintPlugin {
    print(args: { html: string }): Promise<void>;
}


@Injectable()
export class CapacitorPrinterPlugin implements IPrinter {
    name(): string {
        return 'cap-printer';
    }

    isSupported(): boolean {
        return Capacitor.isNativePlatform() && Capacitor.isPluginAvailable('Print');
    }

    print(html: string): Observable<void> {
        return from(print.print({ html })) as Observable<void>;
    }
}
