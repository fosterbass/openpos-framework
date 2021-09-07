import { Injectable } from '@angular/core';
import { from, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { IPrinter } from './printer.interface';

declare module '@capacitor/core' {
    interface PluginRegistry {
        Print: PrintPlugin;
    }
}

interface PrintPlugin {
    print(args: { html: string }): Promise<void>;
}

import { Capacitor, Plugins as CapacitorPlugins } from '@capacitor/core';

@Injectable()
export class CapacitorPrinterPlugin implements IPrinter {
    name(): string {
        return 'cap-printer';
    }

    isSupported(): boolean {
        return Capacitor.isNative && Capacitor.isPluginAvailable('Print');
    }

    print(html: string): Observable<void> {
        return from(CapacitorPlugins.Print.print({ html })) as Observable<void>;
    }
}
