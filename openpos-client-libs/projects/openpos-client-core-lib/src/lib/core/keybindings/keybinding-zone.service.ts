import { Injectable, OnDestroy, Optional } from '@angular/core';
import { KeybindingService } from './keybinding.service';
import { KeybindingEvent } from './keybinding-event.interface';
import { Observable, Subject } from 'rxjs';
import { KeybindingZone } from './keybinding-zone.interface';
import { KeybindingAction } from './keybinding-action.interface';
import { filter, share, takeUntil, tap } from 'rxjs/operators';
import { KeybindingParserService } from './keybinding-parser.service';
import { ActionService } from '../actions/action.service';
import { IActionItem } from '../actions/action-item.interface';

/**
 * Used by UI components/directives to interact with the current KeybindingZone.
 * Each instance is "locked" to a specific zone id (usually the screen or dialog message id).
 */
@Injectable()
export class KeybindingZoneService implements OnDestroy {
    private zoneId: string;
    private unregistered$ = new Subject<any>();

    logActiveEventStyle = 'background-color: lightorange; color: darkorange';

    constructor(private keybindingService: KeybindingService,
                private keybindingParser: KeybindingParserService,
                // The action service may not be available depending on where this service is used
                @Optional() private actionService: ActionService) {
    }

    ngOnDestroy(): void {
        this.unregister();
    }

    register(zoneOrId: KeybindingZone | string): Observable<KeybindingEvent> {
        let zone: KeybindingZone;

        if (typeof zoneOrId === 'string') {
            zone = {
                id: zoneOrId
            };
        } else {
            zone = zoneOrId;
        }

        console.debug(`[KeybindingZoneService]: Setting zone to "${zone.id}" and then registering`);
        this.zoneId = zone.id;
        return this.keybindingService.register({
            ...zone,
            actionService: zone.actionService || this.actionService
        });
    }

    updateZone(updates: any): Observable<KeybindingEvent> {
        return this.keybindingService.updateZone({
            actionsObj: updates,
            actionService: this.actionService,
            id: this.zoneId
        });
    }

    unregister(): void {
        if (this.keybindingService.isRegistered(this.zoneId)) {
            this.keybindingService.unregister(this.zoneId);
        }
        this.unregistered$.next();

    }

    getZoneId(): string {
        return this.zoneId;
    }

    getZone(): KeybindingZone {
        return this.keybindingService.getZone(this.zoneId);
    }

    isRegistered(): boolean {
        return this.keybindingService.isRegistered(this.zoneId);
    }

    activate(): void {
        this.keybindingService.activate(this.zoneId);
    }

    restorePreviousActivation(): void {
        this.keybindingService.restorePreviousActivation();
    }

    deactivate(): void {
        this.keybindingService.deactivate(this.zoneId);
    }

    isActive(): boolean {
        if (!this.zoneId) {
            return false;
        }

        return this.keybindingService.isActive(this.zoneId);
    }

    getNeedActionPayload(): Observable<KeybindingAction> {
        return this.keybindingService.getNeedActionPayload(this.zoneId);
    }

    getKeyDownActionEvent(): Observable<KeybindingEvent> {
        return this.keybindingService.getKeyDownZoneActionEvent(this.zoneId);
    }

    getKeyDownEvent(key?: string): Observable<KeybindingEvent> {
        console.debug(`[KeybindingZoneService]: Getting filtered "allKeyDownEvents()" observable for key(s) "${key}" in zone "${this.zoneId}"`);

        return this.keybindingService.getAllKeyDownEvents(key)
            .pipe(
                tap(event => console.debug(`[KeybindingZoneService]: Received keydown event in zone "${this.zoneId}"`, event, this)),
                filter(event => !!event.zone),
                filter(event => this.zoneId === event.zone.id),
                tap(event => this.logEvent(event)),
                share(),
                takeUntil(this.unregistered$)
            );
    }

    removeKeybinding(key: string): IActionItem {
        return this.keybindingService.removeKeybinding(this.zoneId, key);
    }

    findActionByKey(key: string): IActionItem {
        return this.keybindingService.findActionByKey(this.zoneId, key);
    }

    logEvent(event: KeybindingEvent): void {
        const key = this.keybindingParser.getNormalizedKey(event.domEvent);

        console.group('%c[KeybindingZoneService]', this.logActiveEventStyle);
        console.log(`Key: ${key}`);
        console.log('Zone: ', event.zone);
        console.groupEnd();
    }
}
