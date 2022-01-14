import { Injectable, OnDestroy } from '@angular/core';
import { EMPTY, Observable, Subscription } from 'rxjs';
import { IActionItem } from '../../core/actions/action-item.interface';
import { Keybinding } from './keypress.provider';

@Injectable()
export class DisabledKeyPressProvider implements OnDestroy {
    constructor() {
        console.log('[DisabledKeyPressProvider]: The KeyPressProvider is disabled');
    }

    globalSubscribe(actions: IActionItem[] | IActionItem): Observable<IActionItem> {
        return EMPTY;
    }

    shouldRunGlobalAction(action: IActionItem): boolean {
        return false;
    }

    findMatchingAction(actions: IActionItem[], event: KeyboardEvent): IActionItem {
        return null;
    }

    areEqual(keyBindingA: Keybinding, keyBindingB: Keybinding): boolean {
        return false;
    }

    ngOnDestroy(): void {

    }

    registerKeyPressSource(source$: Observable<KeyboardEvent>) {

    }

    isElementInForm(element: HTMLElement): boolean {
        return false;
    }

    unregisterKeyPressSource(source$: Observable<KeyboardEvent>) {

    }

    subscribe(
        keyOrActionList: string | string[] | IActionItem | IActionItem[],
        priority: number, next: (keyEvent: KeyboardEvent, actionItem?: IActionItem) => void,
        stop$?: Observable<any>, eventType$?: string
    ): Subscription {
        return EMPTY.subscribe();
    }

    registerKeyBindings(
        keyBindings: Keybinding[],
        action: IActionItem,
        priority: number,
        next: (keyEvent: KeyboardEvent, actionItem?: IActionItem) => void,
        eventType: string
    ): Subscription[] {
        return [EMPTY.subscribe()];
    }

    keyHasSubscribers(obj: KeyboardEvent): boolean {
        return false;
    }

    rebuildKeyPressObserver() {

    }

    getNormalizedKey(obj: KeyboardEvent | Keybinding | string): string {
        return '';
    }

    parse(key: string): Keybinding[] {
        return [];
    }

    splitKeys(keys: string): string[] {
        return [];
    }

    unescapeKey(key: string): string {
        return key;
    }

    escapeKey(key: string): string {
        return key;
    }
}
