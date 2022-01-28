import { Injectable, OnDestroy, Optional } from '@angular/core';
import { KeybindingService } from './keybinding.service';
import { KeybindingEvent } from './keybinding-event.interface';
import { EMPTY, Observable, Subject } from 'rxjs';
import { KeybindingZone } from './keybinding-zone.interface';
import { KeybindingAction } from './keybinding-action.interface';
import { filter, takeUntil, tap } from 'rxjs/operators';
import { KeybindingParserService } from './keybinding-parser.service';
import { ActionService } from '../actions/action.service';
import { IActionItem } from '../actions/action-item.interface';
import { KeybindingLikeKey } from './keybinding-like-key.interface';
import { KeybindingPendingAction } from './keybinding-pending-action.interface';
import { CONFIGURATION } from '../../configuration/configuration';

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
        if (!CONFIGURATION.enableKeybinds) {
            return EMPTY;
        }

        const zone = typeof zoneOrId === 'string' ? {id: zoneOrId} : zoneOrId;

        if (!zone) {
            return EMPTY;
        }

        console.log(`[KeybindingZoneService]: Setting my zone to "${zone.id}" and then registering`);
        this.zoneId = zone.id;
        return this.keybindingService.register({
            ...zone,
            actionService: zone.actionService || this.actionService
        });
    }

    updateZone(updates: any): Observable<KeybindingEvent> {
        if (!CONFIGURATION.enableKeybinds) {
            return EMPTY;
        }

        console.log(`[KeybindingZoneService]: Updating my zone "${this.zoneId}"`);
        return this.keybindingService.updateZone({
            ...this.getZone(),
            actionsObj: updates
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

    getNeedActionPayload(keybindingKey?: string): Observable<KeybindingAction> {
        return this.keybindingService.getNeedActionPayload(this.zoneId, keybindingKey);
    }

    getShouldDoAction(): Observable<KeybindingPendingAction> {
        return this.keybindingService.getShouldDoAction(this.zoneId);
    }

    getKeyDownActionEvent(key?: string): Observable<KeybindingEvent> {
        return this.keybindingService.getKeyDownZoneActionEvent(this.zoneId, key);
    }

    getKeyDownEvent(key?: string): Observable<KeybindingEvent> {
        return this.keybindingService.getAllKeyDownEvents(key)
            .pipe(
                filter(event => !!event.zone),
                filter(event => this.zoneId === event.zone.id),
                tap(event => this.logEvent(event)),
                takeUntil(this.unregistered$)
            );
    }

    removeAllKeybindings(keysOrKeybindingsOrObj: (string | IActionItem)[] | any): IActionItem[] {
        return this.keybindingService.removeAllKeybindings(this.zoneId, keysOrKeybindingsOrObj);
    }

    removeKeybinding(keyOrKeybinding: string | IActionItem): IActionItem {
        return this.keybindingService.removeKeybinding(this.zoneId, keyOrKeybinding);
    }

    addAllKeybindings(keybindingObjOrArray: IActionItem[] | any): void {
        this.keybindingService.addAllKeybindings(this.zoneId, keybindingObjOrArray);
    }

    addKeybinding(keybinding: IActionItem): void {
        this.keybindingService.addKeybinding(this.zoneId, keybinding);
    }

    findActionByKey(key: string): IActionItem {
        return this.keybindingService.findActionByKey(this.zoneId, key);
    }

    hasKey(obj: KeybindingLikeKey, key: string): boolean {
        return this.keybindingParser.hasKey(obj, key);
    }

    logEvent(event: KeybindingEvent): void {
        const key = this.keybindingParser.getNormalizedKey(event.domEvent);

        console.group('%c[KeybindingZoneService]', this.logActiveEventStyle);
        console.log(`Key: ${key}`);
        console.log(`Zone Action: ${event.action?.action}`);
        console.log(`Zone Action Payload: ${event.actionPayload}`);
        console.groupEnd();
    }
}
