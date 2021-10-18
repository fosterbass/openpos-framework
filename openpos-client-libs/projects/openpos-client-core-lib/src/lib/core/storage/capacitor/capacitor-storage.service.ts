import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { Storage } from '@capacitor/storage';
import { from, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { StorageContainer } from '../storage-container';

@Injectable({
    providedIn: 'root'
})
export class CapacitorStorageService implements StorageContainer {
    name(): string {
        return 'cap-storage';
    }

    isSupported(): boolean {
        return Capacitor.isNativePlatform() && Capacitor.isPluginAvailable('Storage');
    }

    initialize(): Observable<string> {
        return of('initialized Capacitor storage plugin');
    }

    getValue(key: string): Observable<string> {
        return from(Storage.get({ key })).pipe(
            map(result => result.value)
        );
    }

    setValue(key: string, value: string): Observable<void> {
        return from(Storage.set({ key, value }));
    }

    remove(key: string): Observable<void> {
        return from(Storage.remove({ key }));
    }
}
