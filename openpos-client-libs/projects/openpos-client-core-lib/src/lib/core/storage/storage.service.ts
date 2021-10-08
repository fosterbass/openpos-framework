import { Inject, Injectable, Optional } from '@angular/core';
import { Observable, of } from 'rxjs';
import {flatMap, map, switchMap, take} from 'rxjs/operators';
import { LocalStorageService } from './local-storage/local-storage.service';
import { STORAGE_CONTAINERS, StorageContainer } from './storage-container';

@Injectable({
    providedIn: 'root'
})
export class Storage {
    private readonly container$: Observable<StorageContainer>

    constructor(
        @Inject(STORAGE_CONTAINERS) @Optional() containers: StorageContainer[],

        // local storage should always be available as a fallback...
        localStorage: LocalStorageService
    ) {
        let container: StorageContainer = localStorage;

        console.log(`storage containers: ${containers}`);

        if (containers) {
            console.log(`storage containers.length: ${containers.length}`);
            const supportedContainers = containers.filter(c => c.isSupported());
            console.log(`supportedContainers.length: ${supportedContainers.length}`);
            container = supportedContainers.length > 0 ? supportedContainers[0] : localStorage;
        }

        console.log(`using storage container: ${container.name()}`);
        this.container$ = of(container);
    }

    isAvailable(): Observable<boolean> {
        return this.container$.pipe(
            flatMap(container => container.isAvailable())
        );
    }

    getValue(key: string): Observable<string> {
        return this.container$.pipe(
            take(1),
            switchMap(container => container.getValue(key))
        );
    }

    setValue(key: string, value: string): Observable<void> {
        return this.container$.pipe(
            take(1),
            switchMap(container => container.setValue(key, value))
        );
    }

    remove(key: string): Observable<void> {
        return this.container$.pipe(
            take(1),
            switchMap(container => container.remove(key))
        );
    }

    clear(): Observable<void> {
        return this.container$.pipe(
            take(1),
            switchMap(container => container.clear())
        )
    }
}