import { Injectable, OnDestroy } from '@angular/core';
import { SessionService } from '../services/session.service';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { KeybindingService } from './keybinding.service';
import { MessageTypes } from '../messages/message-types';

/**
 * Manages enabling/disabling keybindings when the screen is locked/unlocked
 */
@Injectable({
    providedIn: 'root'
})
export class KeybindingLockScreenService implements OnDestroy {
    private destroyed$ = new Subject();

    constructor(private sessionService: SessionService,
                private keybindingService: KeybindingService) {
        this.sessionService.getMessages(
            MessageTypes.LOCK_SCREEN,
            MessageTypes.UNLOCK_SCREEN
        ).pipe(
            takeUntil(this.destroyed$)
        ).subscribe(message => this.handleLockScreenMessage(message));

        console.log('[KeybindingLockScreenService]: Listening for message types "LockScreen" and "UnlockScreen"');
    }

    handleLockScreenMessage(message: any): void {
        if (message.type === MessageTypes.LOCK_SCREEN) {
            console.log('[KeybindingLockScreenService]: Disabling keybinding because the screen is locked');
            this.keybindingService.disable();
        } else if (message.type === MessageTypes.UNLOCK_SCREEN) {
            console.log('[KeybindingLockScreenService]: Enabling keybinding because the screen is unlocked');
            this.keybindingService.enable();
        }
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
    }
}
