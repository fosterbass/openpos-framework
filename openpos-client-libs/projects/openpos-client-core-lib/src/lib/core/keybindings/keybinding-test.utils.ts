import { ConnectableObservable, Observable, Subject } from 'rxjs';
import { filter, publishReplay, tap } from 'rxjs/operators';
import { MessageTypes } from '../messages/message-types';
import { Injectable } from '@angular/core';

@Injectable()
export class MockSessionService {
    private messages$ = new Subject<any>();
    public screenMessage$: Observable<any>;
    public dialogMessage$: Observable<any>;

    public isRunningInBrowser = jasmine.createSpy('isRunningInBrowser').and.returnValue(true);

    constructor() {
        this.screenMessage$ = this.getMessages(MessageTypes.SCREEN)
            .pipe(
                // Mimic the behavior of the real SessionService
                publishReplay(1)
            );
        this.dialogMessage$ = this.getMessages(MessageTypes.DIALOG)
            .pipe(
                // Mimic the behavior of the real SessionService
                publishReplay(1)
            );

        (this.screenMessage$ as ConnectableObservable<any>).connect();
        (this.dialogMessage$ as ConnectableObservable<any>).connect();
    }

    public getMessages(...types: string[]): Observable<any> {
        return this.messages$
            .pipe(
                tap(message => console.debug(`[MockSessionService]: Filtering for message types "${types.join(', ')}"`, message)),
                filter(message => !types || types.includes(message.type)),
                tap(message => console.debug(`[MockSessionService]: Processing message of type "${message.type}"`, message))
            );
    }

    public dispatchMessage(message: any): void {
        console.debug(`[MockSessionService]: Dispatching message "${message.id}" of type "${message.type}"`, message);
        this.messages$.next(message);
    }
}

@Injectable()
export class MockActionService {
    private actionsDisabledState: { [key: string]: boolean } = {};

    public doAction = jasmine.createSpy('doAction');
    public actionBlocked = jasmine.createSpy('actionBlocked').and.returnValue(false);

    public updateActionDisabledState(actionName: string, disabled: boolean): void {
        console.debug(`[MockActionService]: Setting "${actionName}" to "disabled = ${disabled}`, this.actionsDisabledState);
        this.actionsDisabledState[actionName] = disabled;
    }

    public actionIsDisabled(actionName: string): boolean {
        console.debug(`[MockActionService]: Check "${actionName}" disabled state`, this.actionsDisabledState);
        return this.actionsDisabledState[actionName] === true;
    }
}

export class KeybindingTestUtils {
    static pressKey(initObj: KeyboardEventInit | string): boolean {
        if (typeof initObj === 'string') {
            initObj = {
                key: initObj
            };
        }
        console.log('[KeybindingTestUtils]: Pressing key', initObj);
        return window.dispatchEvent(new KeyboardEvent('keydown', initObj));
    }

    static createUserZone(actionService?: any) {
        return {
            id: 'user',
            actionsObj: {
                logout: {
                    action: 'Logout',
                    keybind: 'Shift+Cmd+Q',
                    // Here for convenience
                    event: {
                        key: 'Q',
                        shiftKey: true,
                        metaKey: true
                    }
                },
                killUser: {
                    action: 'KillUser',
                    keybind: 'Ctrl+Cmd+Q',
                    // Here for convenience
                    event: {
                        key: 'Q',
                        ctrlKey: true,
                        metaKey: true
                    }
                }
            },
            actionService
        };
    }

    static createBaconStripZone(actionService?: any) {
        return {
            id: 'bacon-strip',
            actionsObj: {
                stealItem: {
                    action: 'StealItem',
                    keybind: 'F3',
                    // Here for convenience
                    event: {
                        key: 'F3'
                    }
                },
                punchCashier: {
                    action: 'PunchCashier',
                    keybind: 'F4',
                    // Here for convenience
                    event: {
                        key: 'F4'
                    }
                }
            },
            actionService
        };
    }

    static createSaleZone(actionService?: any) {
        return {
            id: 'sale',
            actionsObj: {
                runAway: {
                    keybind: 'Escape',
                    action: 'Back',
                    // Here for convenience
                    event: {
                        key: 'F7'
                    }
                },
                throwItem: {
                    action: 'ThrowItem',
                    keybind: 'F7',
                    // Here for convenience
                    event: {
                        key: 'F7'
                    }
                },
                punchLoyaltyCustomer: {
                    action: 'PunchLoyaltyCustomer',
                    keybind: 'F8',
                    // Here for convenience
                    event: {
                        key: 'F7'
                    }
                }
            },
            actionService
        };
    }
}
