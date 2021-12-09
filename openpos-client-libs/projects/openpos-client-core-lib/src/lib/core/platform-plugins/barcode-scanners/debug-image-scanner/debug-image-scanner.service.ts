import { Observable } from 'rxjs';
import { ImageScanner, ScanData, ScanDataType, ScannerViewRef } from '../scanner';
import { Injectable } from '@angular/core';

declare global {
    interface Console {
        scanFromImageScanner?: (type: ScanDataType, data: string) => void;
    }
}

@Injectable()
export class DebugImageScanner implements ImageScanner {
    name(): string {
        return 'Debug';
    }

    beginScanning(view: ScannerViewRef): Observable<ScanData> {
        return new Observable(observer => {
            const subscription = view.viewChanges().subscribe({
                next: value => {
                    console.log('scanner view container changed', value);
                }
            });

            console.scanFromImageScanner = (type, data) => {
                observer.next({
                    type,
                    data
                });
            };

            return () => {
                console.scanFromImageScanner = undefined;
                subscription.unsubscribe();
                console.log('scan data stopped');
            };
        });
    }
}
