import { Injectable, OnDestroy } from '@angular/core';
import { EMPTY, fromEvent, merge, Observable, Subject } from 'rxjs';
import { filter, map, share, takeUntil, tap } from 'rxjs/operators';
import { CONFIGURATION } from '../../configuration/configuration';
import { KeybindingZone } from './keybinding-zone.interface';
import { KeybindingEvent } from './keybinding-event.interface';
import { IActionItem } from '../actions/action-item.interface';
import { KeybindingParserService } from './keybinding-parser.service';
import { KeybindingActionCache } from './keybinding-action-cache.interface';
import { KeybindingPropertyCrawlerService } from './keybinding-property-crawler.service';
import { KeybindingAction } from './keybinding-action.interface';
import { KeybindingKey } from './keybinding-key.interface';

/**
 * The core (singleton) service responsible for the keybinding functionality.
 * This ensures only one keybinding zone is active at a time and automatically executes actions for their
 * corresponding keybindings.
 */
@Injectable({
    providedIn: 'root',
})
export class KeybindingService implements OnDestroy {
    private keyDownEvent$: Observable<KeyboardEvent>;
    private zoneActivated$ = new Subject<KeybindingZone>();
    private zoneDeactivated$ = new Subject<KeybindingZone>();
    private needActionPayload$ = new Subject<KeybindingAction>();
    private destroyed$ = new Subject();

    private enabled = true;
    private activeZone: KeybindingZone;
    private activeZoneHistory: string[] = [];
    private maxActiveZoneHistory = 100;

    private zones: { [key: string]: KeybindingZone } = {};
    private keyDownZoneActionEvents: { [key: string]: Observable<KeybindingEvent> } = {};
    private stopZoneKeyDown: { [key: string]: Subject<any> } = {};
    private zoneActionsCache: { [zoneId: string]: KeybindingActionCache } = {};

    logActiveEventStyle = 'background-color: lightgreen; color: darkgreen';

    constructor(private keybindingParser: KeybindingParserService,
                private propertyCrawler: KeybindingPropertyCrawlerService) {
        // Capture events, instead of letting them bubble, because some Angular Material components
        // stop propagation of keys they handle and we want to know about every key that's pressed
        this.keyDownEvent$ = fromEvent<KeyboardEvent>(window, 'keydown', {capture: true})
            .pipe(
                tap(event => console.debug('[KeybindingService]: Received keydown event in root observable', event, this)),
                filter(() => this.enabled),
                filter(() => CONFIGURATION.enableKeybinds),
                share(),
                takeUntil(this.destroyed$)
            );
    }

    ngOnDestroy(): void {
        console.log('[KeybindingService]: Service is destroyed');
        this.destroyed$.next();
    }

    enable(): void {
        this.enabled = true;
        console.log('[KeybindingService]: Listening for events');
    }

    isEnabled(): boolean {
        return this.enabled;
    }

    disable(): void {
        this.enabled = false;
        console.log('[KeybindingService]: Stopped listening for events');
    }

    register(zone: KeybindingZone): Observable<KeybindingEvent> {
        if (zone.actionsObj) {
            zone = {
                ...zone,
                actions: this.propertyCrawler.findKeybindings(zone.actionsObj)
            };
        }

        console.log(`[KeybindingService]: Registered zone "${zone.id}"`, zone);
        const stopZoneKeyDown$ = new Subject<any>();

        console.log(`[KeybindingService]: Creating observable for executing keybinding actions in zone "${zone.id}"`, zone);
        const keyDownActionEvent$ = this.keyDownEvent$.pipe(
            map(event => this.createKeybindingEvent(zone.id, event)),
            tap(event => console.debug(`[KeybindingService]: Received keydown event in zone observable "${event.zone.id}"`, event, this)),
            filter(event => this.isActive(event.zone.id)),
            tap(event => this.doEventAction(event)),
            tap(event => this.logEvent(event)),
            share(),
            takeUntil(merge(stopZoneKeyDown$, this.destroyed$))
        );

        this.zones[zone.id] = zone;
        this.stopZoneKeyDown[zone.id] = stopZoneKeyDown$;
        this.keyDownZoneActionEvents[zone.id] = keyDownActionEvent$;
        this.cacheActionsByKey(zone.id);

        // Make it hot because we want to listen even if nobody else is
        keyDownActionEvent$.subscribe();
        return keyDownActionEvent$;
    }

    updateZone(zone: KeybindingZone): Observable<KeybindingEvent> {
        if (!this.zones[zone.id]) {
            console.warn(`[KeybindingService]: Not updating zone because the  id "${zone.id}" does not exist`);
            return;
        }

        if (zone.actionsObj) {
            zone = {
                ...zone,
                actions: this.propertyCrawler.findKeybindings(zone.actionsObj)
            };
        }

        console.log(`[KeybindingService]: Updated zone "${zone.id}"`, zone);
        this.zones[zone.id] = zone;
        this.cacheActionsByKey(zone.id);

        return this.keyDownZoneActionEvents[zone.id];
    }

    unregister(zoneId: string): KeybindingZone {
        const zone = this.zones[zoneId];

        if (!zone) {
            console.warn(`[KeybindingService]: Cannot unregister "${zoneId}" because the id has not been registered`);
            return null;
        }

        if (this.isActive(zone.id)) {
            this.deactivate(zone.id);
        }

        this.stopZoneKeyDown[zone.id].next();

        delete this.zones[zoneId];
        delete this.keyDownZoneActionEvents[zoneId];
        delete this.zoneActionsCache[zoneId];
        delete this.stopZoneKeyDown[zone.id];

        console.log(`[KeybindingService]: Unregistered "${zoneId}"`, zone);

        return zone;
    }

    addToActivationHistory(zoneId: string): void {
        if (this.activeZoneHistory.length === this.maxActiveZoneHistory) {
            this.activeZoneHistory.shift();
        }

        this.activeZoneHistory.push(zoneId);
    }

    getPreviousActiveZoneId(): string {
        return this.activeZoneHistory[this.activeZoneHistory.length - 2];
    }

    doEventAction(event: KeybindingEvent): void {
        if (!this.shouldDoEventAction(event)) {
            console.log('[KeybindingService]: Not executing event action (see previous debug log entries for reason)', event);
            return;
        }

        const keybindingAction: KeybindingAction = {
            action: event.action,
            payload: null
        };
        // Provide an opportunity to add a payload before executing the action
        this.needActionPayload$.next(keybindingAction);

        console.log(`%c[KeybindingService]: Executing action ${keybindingAction.action.action}`, this.logActiveEventStyle, event);
        event.zone.actionService?.doAction(keybindingAction.action, keybindingAction.payload);
        event.didDoAction = true;
    }

    isEventActionEnabled(event: KeybindingEvent): boolean {
        let enabled = event.action.enabled !== false;
        const actionService = event.zone.actionService;

        if (actionService) {
            enabled = !actionService.actionIsDisabled(event.action.action);
        }

        return enabled;
    }

    isRegistered(zoneId: string): boolean {
        return !!this.zones[zoneId];
    }

    getZone(zoneId: string): KeybindingZone {
        return this.zones[zoneId];
    }

    isActive(zoneId: string): boolean {
        const zone = this.zones[zoneId];

        if (!zone) {
            console.warn(`[KeybindingService]: There is no zone with id "${zoneId}"`);
            return false;
        }

        const isZoneActive = zone.alwaysActive || (this.getActiveZoneId() === zone.id);
        console.debug(`[KeybindingService]: Zone "${zoneId}" is ${!isZoneActive ? 'not' : ''} active`);
        return isZoneActive;
    }

    hasActiveZone(): boolean {
        return !!this.getActiveZoneId();
    }

    getActiveZoneId(): string {
        return this.activeZone ? this.activeZone.id : null;
    }

    activate(zoneId: string): void {
        if (this.hasActiveZone()) {
            this.deactivate(this.getActiveZoneId());
        }

        this.activeZone = this.zones[zoneId];

        if (this.activeZone) {
            this.addToActivationHistory(zoneId);
            this.zoneActivated$.next(this.activeZone);
            console.log(`%c[KeybindingService]: Activated zone id "${zoneId}"`, this.logActiveEventStyle, this.activeZone);
        } else {
            console.warn(`[KeybindingService]: There is no keybinding zone with id "${zoneId}"`);
        }
    }

    deactivate(zoneId: string): void {
        if (!this.isRegistered(zoneId)) {
            console.warn(`[KeybindingService]: The zone id "${zoneId}" is not registered`, this.activeZone);
        } else if (!this.activeZone) {
            console.warn(`[KeybindingService]: There is no active zone to deactivate`, this.zones);
        } else if (this.getActiveZoneId() !== zoneId) {
            console.warn(`[KeybindingService]: The zone id "${zoneId}" is not active`, this.activeZone);
        } else {
            this.zoneDeactivated$.next(this.activeZone);
            this.activeZone = null;
            console.log(`[KeybindingService]: Deactivated id "${zoneId}"`, this.zones);
        }
    }

    restorePreviousActivation(): void {
        if (this.hasActiveZone()) {
            this.deactivate(this.getActiveZoneId());
        }

        const previousZoneId = this.getPreviousActiveZoneId();

        if (this.isRegistered(previousZoneId)) {
            console.log(`[KeybindingService]: Restoring previous zone id "${previousZoneId}"`, this.zones[previousZoneId]);
            this.activate(previousZoneId);
        } else {
            console.warn(`[KeybindingService]: The previous zone id "${previousZoneId}" is no longer registered`, previousZoneId);
        }
    }

    removeKeybinding(zoneId: string, key: string): IActionItem {
        const zone = this.zones[zoneId];

        if (!zone) {
            console.log(`[KeybindingService]: Cannot find zone "${zoneId}" to remove action ${key}`, this.zones);
            return null;
        }

        const actionIndex = zone.actions.findIndex(action => this.keybindingParser.areEqual(action.keybind, key));

        if (actionIndex < 0) {
            return null;
        }

        const actionItem = this.zones[zoneId].actions.splice(actionIndex, 1)[0];
        console.log(`[KeybindingService]: Removed keybinding ${actionItem.keybind} from zone ${zone.id}`, actionItem, zone);
        this.cacheActionsByKey(zone.id);

        return actionItem;
    }

    createKeybindingEvent(zoneId: string, event: KeyboardEvent): KeybindingEvent {
        return {
            domEvent: event,
            zone: this.zones[zoneId],
            action: this.findActionByKey(zoneId, event),
            didDoAction: false
        };
    }

    shouldDoEventAction(event: KeybindingEvent): boolean {
        if (!this.isActive(event.zone.id)) {
            console.debug(`[KeybindingService]: Not executing action because the zone "${event.zone.id}" is not active`, event);
            return false;
        }

        if (!event.action) {
            console.debug(`[KeybindingService]: Not executing action because there is no action for the keybinding`, event);
            return false;
        }

        if (event.zone.autoDoAction === false) {
            console.debug(`[KeybindingService]: Not executing action ${event.action.action} because "autoDoAction=false"`, event);
            return false;
        }

        if (!this.isEventActionEnabled(event)) {
            console.debug(`[KeybindingService]: Not executing action ${event.action.action} because it is disabled`, event);
            return false;
        }

        if (!event.zone.actionService) {
            console.debug(`[KeybindingService]: Not executing action ${event.action.action} ` +
                `because there is no ActionService set in the "${event.zone.id}" zone`, event);
            return false;
        }

        return true;
    }

    findActionByKey(zoneId: string, obj: KeyboardEvent | KeybindingKey | string): IActionItem {
        if (!this.zones[zoneId]) {
            return null;
        }

        const key = this.keybindingParser.getNormalizedKey(obj);
        const zoneActionByKeyCache = this.zoneActionsCache[zoneId];
        return zoneActionByKeyCache ? zoneActionByKeyCache[key] : null;
    }

    cacheActionsByKey(zoneId: string): void {
        const zone = this.zones[zoneId];

        if (!zone) {
            console.warn(
                `[KeybindingService]: Not caching actions because there is no zone with id "${zone.id}"`,
                this.zones);
            return;
        }

        const zoneActionByKeyCache: KeybindingActionCache = {};

        if (zone.actions) {
            zone.actions.forEach(action => {
                const actionKey = this.keybindingParser.getNormalizedKey(action.keybind);
                zoneActionByKeyCache[actionKey] = action;
            });
        }

        this.zoneActionsCache[zone.id] = zoneActionByKeyCache;
        console.log(`[KeybindingService]: Cached actions for zone "${zone.id}"`, zoneActionByKeyCache);
    }

    getNeedActionPayload(zoneId: string): Observable<KeybindingAction> {
        return this.needActionPayload$.asObservable()
            .pipe(
                filter(() => zoneId === this.getActiveZoneId()),
                share()
            );
    }

    getZoneActivated(): Observable<KeybindingZone> {
        return this.zoneActivated$.asObservable();
    }

    getZoneDeactivated(): Observable<KeybindingZone> {
        return this.zoneDeactivated$.asObservable();
    }

    getKeyDownZoneActionEvent(zoneId: string): Observable<KeybindingEvent> {
        if (!this.keyDownZoneActionEvents[zoneId]) {
            console.warn(`[KeybindingService]: There is no key down action event for the zone "${zoneId}"`);
            return EMPTY;
        }

        return this.keyDownZoneActionEvents[zoneId];
    }

    getAllKeyDownEvents(key?: string): Observable<KeybindingEvent> {
        return this.keyDownEvent$
            .pipe(
                map(event => this.createKeybindingEvent(this.getActiveZoneId(), event)),
                tap(event => console.debug('[KeybindingService]: All Key Down Event', event)),
                filter(event => this.doesKeyboardEventMatchKey(event.domEvent, key)),
                share()
            );
    }

    doesKeyboardEventMatchKey(event: KeyboardEvent, key: string): boolean {
        if (!key) {
            console.debug('[KeybindingService]: Returning successful match because no key was specified to filter', event);
            return true;
        }

        const hasKey = this.keybindingParser.hasKey(event, key);
        console.debug(`[KeybindingService]: Key ${hasKey ? 'matches' : 'does not match'}`, event, key);
        return hasKey;
    }

    logEvent(event: KeybindingEvent): void {
        const key = this.keybindingParser.getNormalizedKey(event.domEvent);
        const action = this.findActionByKey(event.zone.id, event.domEvent);

        // console.groupCollapsed(`%c[KeybindingService]: ${event.domEvent.type}: ${key}, action: ${actionName}`, this.logEventStyle);
        console.group('%c[KeybindingService]', event.didDoAction ? this.logActiveEventStyle : '');
        console.log(`Key: ${key}`);
        console.log('Action: ', action);
        console.log('Did Do Action: ', event.didDoAction);
        console.log('Active Zone: ', this.activeZone);
        console.log('Active Zone History: ', this.activeZoneHistory);
        console.log('Actions Cache: ', this.zoneActionsCache[event.zone.id]);
        console.log('All Zones: ', this.zones);
        console.log('All Actions Cache: ', this.zoneActionsCache);
        console.groupEnd();
    }
}
