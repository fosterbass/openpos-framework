import { Injectable } from '@angular/core';
import { iif, Observable, of, SubscribableOrPromise, Subscription } from 'rxjs';
import { map, filter, bufferToggle, timeout, catchError, windowToggle, tap, mergeAll, publish, refCount } from 'rxjs/operators';
import { WEDGE_SCANNER_ACCEPTED_KEYS } from './wedge-scanner-accepted-keys';
import { DomEventManager } from '../../../services/dom-event-manager.service';
import { Scanner, ScanData, ScanDataType } from '../scanner';
import { ConfigurationService } from '../../../services/configuration.service';
import { WedgeScannerConfigMessage } from '../../../messages/wedge-scanner-config-message';

interface ControlSequence { modifiers: string[]; key: string; }

@Injectable({
    providedIn: 'root'
})
export class WedgeScannerPlugin implements Scanner {
    private startSequence = '*';
    private endSequence = 'Enter';
    private codeTypeLength = 0;
    private timeout = 500;
    private typeMap: Map<string, ScanDataType>;
    private scannerActive: boolean;
    private startSequenceObj = this.getControlStrings(this.startSequence);
    private endSequenceObj = this.getControlStrings(this.endSequence);
    private enabled = true;

    private startScanBuffer: Observable<any>;
    private stopScanBuffer: Observable<any>;
    private keypressBlocker: Subscription;
    private scannerBuffer: Observable<ScanData>;

    private scannerSubscription: Subscription;

    private scanObservable: Observable<ScanData>;

    constructor(private configuration: ConfigurationService, private domEventManager: DomEventManager) {

        // Initialize scan with default setting
        console.debug('Creating scan observable');
        this.scanObservable = this.createScanObservable();
        console.debug('Created scan observable');

        this.configuration.getConfiguration<WedgeScannerConfigMessage>('WedgeScanner').subscribe(m => {
            if (m.startSequence) {
                this.startSequence = m.startSequence;
                this.startSequenceObj = this.getControlStrings(this.startSequence);
            }
            if (m.endSequence) {
                this.endSequence = m.endSequence;
                this.endSequenceObj = this.getControlStrings(this.endSequence);
            }
            if (m.codeTypeLength) {
                this.codeTypeLength = m.codeTypeLength;
            }
            if (m.acceptKeys) {
                m.acceptKeys.forEach(key => {
                    WEDGE_SCANNER_ACCEPTED_KEYS.push(key);
                });
            }
            if (m.timeout) {
                this.timeout = m.timeout;
            }
            if (!!m.enabled) {
                this.enabled = m.enabled;
            }

            console.debug('Received config message for WedgeScanner.Re-Creating scan observable');
            // Re-Initialize the scan with updated config received from server
            this.scanObservable = this.createScanObservable();
            console.debug('Received config message for WedgeScanner.Re-Created scan observable');
        });

        this.configuration.getConfiguration('WedgeScannerTypes').subscribe(m => {
            this.typeMap = new Map<string, ScanDataType>();
            Object.getOwnPropertyNames(m).forEach(element => {
                this.typeMap.set(element, m[element]);
            });
        });
    }


    createScanObservable(): Observable<ScanData> {
        return iif(
            () => this.enabled,
            // true
            new Observable(observer => {
                this.scannerActive = true;
                if (this.scannerSubscription) {
                    this.scannerSubscription.unsubscribe();
                }
                this.scannerSubscription = this.createScanBuffer().subscribe({
                    next: d => observer.next(d),
                });
                return () => {
                    this.scannerSubscription.unsubscribe();
                    this.scannerActive = false;
                };
            }).pipe(
                publish(),
                refCount()
            ),
            // false
            new Observable(observer => {
                if (this.scannerSubscription) {
                    this.scannerSubscription.unsubscribe();
                }
                console.log(`WedgeScanner disabled. Not starting.`);
            }).pipe(
                publish(),
                refCount()
            )
        );
    }

    beginScanning(): Observable<ScanData> {
        return this.scanObservable;
    }

    private createScanBuffer(): Observable<ScanData> {
        if (!this.startScanBuffer) {
            this.startScanBuffer = this.domEventManager.createEventObserver(window, 'keydown', { capture: true }).pipe(
                filter((e: KeyboardEvent) => this.filterForControlSequence(this.startSequenceObj, e),
                    tap(e => console.debug(`Starting Scan Capture: ${e}`))));
        }
        if (!this.stopScanBuffer) {
            this.stopScanBuffer = this.domEventManager.createEventObserver(window, 'keydown', { capture: true }).pipe(
                timeout(this.timeout),
                filter((e: KeyboardEvent) => this.filterForControlSequence(this.endSequenceObj, e)),
                tap(e => console.debug(`Stopping Scan Capture: ${e}`)),
                catchError(e => {
                    console.debug('Scan Capture timed out');
                    return of('timed out');
                }),
            );
        }

        if (!this.keypressBlocker) {
            // This subscription will block keyboard events during a scan
            this.keypressBlocker =
                this.domEventManager.createEventObserver(window, ['keypress', 'keyup', 'keydown'], { capture: true }).pipe(
                    windowToggle(this.startScanBuffer, () => this.stopScanBuffer),
                    mergeAll()
                ).subscribe((e: KeyboardEvent) => {
                    if (e.type === 'keydown') {
                        e.stopPropagation();
                    } else {
                        e.stopImmediatePropagation();
                    }
                    e.preventDefault();
                });
        }

        if (!this.scannerBuffer) {
            // buffer up all keydown events and prevent them from propagating while scanning
            this.scannerBuffer = this.domEventManager.createEventObserver(window, 'keydown', { capture: true }).pipe(
                bufferToggle(
                    this.startScanBuffer,
                    () => this.stopScanBuffer
                ),
                // We need to filter out any incomplete scans
                filter((events: KeyboardEvent[]) => this.filterForControlSequence(this.endSequenceObj, events[events.length - 1])),
                tap(events => console.debug(`Complete Scan: ${events.map(e => e.key).join(', ')}`)),
                map((events: KeyboardEvent[]) => this.convertKeyEventsToChars(events)),
                // Join the buffer into a string and remove the start and stop characters
                map((s) => s.join('')),
                map((s: string) => this.getScanData(s))
            );
        }

        return this.scannerBuffer;
    }

    private getControlStrings(sequence: string): ControlSequence {
        let modifiers: string[];
        let key: string;

        if (sequence.includes('+')) {
            modifiers = sequence.split('+');
            modifiers = modifiers.slice(0, modifiers.length - 1);
            key = sequence.slice(sequence.lastIndexOf('+') + 1)[0];
        } else {
            key = sequence;
        }

        return { modifiers, key };
    }

    private filterForControlSequence(sequence: ControlSequence, e: KeyboardEvent): boolean {
        if (!this.scannerActive) {
            return false;
        }
        const keyPressed = e.key === sequence.key;
        console.debug(`Start/Stop key (${e.key}) pressed: ${keyPressed} `);
        if (!!sequence.modifiers) {
            const modifiersPressed = sequence.modifiers.map(m => this.checkModifier(e, m)).reduce((accum, m) => accum && m);
            console.debug(`Start/Stop Modifiers (${sequence.modifiers.join(', ')}) pressed: ${modifiersPressed}`);
            return modifiersPressed && keyPressed;
        }
        return keyPressed;
    }

    private checkModifier(e: KeyboardEvent, modifier: string): boolean {
        switch (modifier) {
            case 'ctrl':
                return e.ctrlKey;
            case 'alt':
                return e.altKey;
        }
    }

    private convertKeyEventsToChars(events: KeyboardEvent[]): string[] {
        // We need to look for 4 character sequences with the alt key pressed and convert them into the
        // special characters they represent
        // We also want to filter out keys that are not in our list of accepted keys
        const charList = [];
        for (let i = 0; i < events.length; i++) {
            const e = events[i];

            // if the first key is the start key skip it
            if (i === 0 && this.filterForControlSequence(this.startSequenceObj, events[i])) {
                continue;
            }

            // if the first key is the end key skip it
            if (i === events.length - 1 && this.filterForControlSequence(this.endSequenceObj, events[i])) {
                continue;
            }

            if (e.altKey && i < events.length - 1) {
                // get the next number
                const e1 = events[i + 1];
                const e2 = events[i + 2];
                const e3 = events[i + 3];
                const e4 = events[i + 4];

                // convert the char code into a string
                charList.push(String.fromCharCode(parseInt(e1.key + e2.key + e3.key + e4.key, 10)));

                // skip the next value since we already accounted for it.
                i += 4;
            } else if (WEDGE_SCANNER_ACCEPTED_KEYS.includes(e.key)) {
                let key = e.key;
                if (key === 'Enter') {
                    key = '\n';
                }
                charList.push(key);
            }
        }

        return charList;
    }

    public stripWedgeControlCharacters(data: string): string {
        console.log(`Stripping wedge control characters from ${data}`);
        if (!this.isCommandKey(this.endSequence) && data.endsWith(this.endSequence)) {
            data = data.slice(0, data.length - this.endSequence.length);
        }
        if (!this.isCommandKey(this.startSequence) && data.startsWith(this.startSequence)) {
            data = data.slice(this.startSequence.length, data.length);
        }
        return data;
    }

    private isCommandKey(key: string) {
        if (!!key) {
            key = key.toLowerCase();
        }
        return 'enter' === key || 'tab' === key || 'ctrl' === key || 'alt' === key;
    }

    private getScanData(s: string): ScanData {
        const type = s.slice(0, this.codeTypeLength);
        const scanData: ScanData = { data: s.slice(this.codeTypeLength) };
        if (!!this.typeMap && this.typeMap.has(type)) {
            scanData.type = this.typeMap.get(type);
        }
        if (!!type && type !== '') {
            scanData.rawType = type;
        }
        return scanData;
    }

    public getStartSequence(): string {
        return this.startSequence;
    }
}
