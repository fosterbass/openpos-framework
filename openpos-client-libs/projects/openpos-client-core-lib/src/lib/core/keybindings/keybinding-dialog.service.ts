import { Injectable, OnDestroy } from '@angular/core';
import { KeybindingService } from './keybinding.service';
import { SessionService } from '../services/session.service';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { MessageTypes } from '../messages/message-types';
import { LifeCycleMessage } from '../messages/life-cycle-message';
import { LifeCycleEvents } from '../messages/life-cycle-events.enum';
import { ActionMessage } from '../messages/action-message';
import { KeybindingEvent } from './keybinding-event.interface';
import { CONFIGURATION } from '../../configuration/configuration';

/**
 * The core (singleton) service responsible for the behavior specific to dialogs.
 */
@Injectable({
    providedIn: 'root'
})
export class KeybindingDialogService implements OnDestroy {
    private destroyed$ = new Subject();
    private isDialogOpen = false;
    private enabled = true;

    // Examples: 'Escape', 'Ctrl+Enter', 'Escape,Ctrl+Enter,Delete'
    public dialogCloseKey = 'Escape';
    public dialogCloseAction = 'Close';
    public doNotBlockForResponse = true;

    logActiveEventStyle = 'background-color: lightgreen; color: darkgreen';

    constructor(private keybindingService: KeybindingService, private sessionService: SessionService) {
        this.listenForLifeCycleEvents();
        this.listenForKeyDownEvents();
    }

    listenForLifeCycleEvents(): void {
        this.sessionService.getMessages(MessageTypes.LIFE_CYCLE_EVENT)
            .pipe(
                filter(() => CONFIGURATION.enableKeybinds),
                filter(() => this.enabled),
                takeUntil(this.destroyed$)
            ).subscribe(message => this.handleLifeCycleEvent(message));
    }

    listenForKeyDownEvents(): void {
        this.keybindingService.getAllKeyDownEvents()
            .pipe(
                filter(() => CONFIGURATION.enableKeybinds),
                filter(() => this.enabled),
                filter(event => this.shouldCloseDialog(event)),
                takeUntil(this.destroyed$)
            ).subscribe(() => this.closeDialog());
    }

    handleLifeCycleEvent(message: LifeCycleMessage) {
        switch (message.eventType) {
            case LifeCycleEvents.DialogClosing:
                this.isDialogOpen = false;
                break;
            case LifeCycleEvents.DialogOpening:
                this.isDialogOpen = true;
        }
    }

    shouldCloseDialog(event: KeybindingEvent): boolean {
        if (!this.isDialogOpen) {
            return false;
        }

        if (!this.keybindingService.doesMatchKey(event.domEvent, this.dialogCloseKey)) {
            return false;
        }

        const activeZoneId = this.keybindingService.getActiveZoneId();
        const closeAction = this.keybindingService.findActionByKey(activeZoneId, event.domEvent);

        // Only close when there's not an existing action to handle this key press
        return !closeAction;
    }

    enable(): void {
        this.enabled = true;
        console.log('[KeybindingDialogService]: Is enabled and listening for events');
    }

    disable(): void {
        this.enabled = false;
        console.log('[KeybindingDialogService]: Stopped listening for events');
    }

    isEnabled(): boolean {
        return this.enabled;
    }

    closeDialog(): void {
        console.log(`%c[KeybindingDialogService]: Closing dialog with action "${this.dialogCloseAction}"`, this.logActiveEventStyle);
        this.sessionService.sendMessage(
            new ActionMessage(this.dialogCloseAction, this.doNotBlockForResponse)
        );
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
    }
}
