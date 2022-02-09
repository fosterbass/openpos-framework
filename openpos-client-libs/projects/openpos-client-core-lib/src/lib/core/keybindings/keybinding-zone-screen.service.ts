import { Injectable, OnDestroy } from '@angular/core';
import { SessionService } from '../services/session.service';
import { debounce, filter, takeUntil } from 'rxjs/operators';
import { merge, of, Subject } from 'rxjs';
import { KeybindingZone } from './keybinding-zone.interface';
import { KeybindingZoneService } from './keybinding-zone.service';
import { KeybindingService } from './keybinding.service';
import { CONFIGURATION } from '../../configuration/configuration';

/**
 * Manages the automatic activating/deactivating keybinding zones for screens and dialogs
 */
@Injectable()
export class KeybindingZoneScreenService implements OnDestroy {
    private messageId: string;
    private messageType: string;
    private destroyed$ = new Subject();
    private stop$ = new Subject();

    public dialogMessageDebounceTime = 10;

    constructor(private sessionService: SessionService,
                private keybindingService: KeybindingService,
                private keybindingZoneService: KeybindingZoneService) {
    }

    start(messageType: string): void {
        this.messageType = messageType;

        // In case we subscribe too late we want to explicitly listen to these UI message observables because they publishReplay
        const screenMessages$ = merge(
            this.sessionService.screenMessage$,
            // Dialog messages can fire more than once for the same message, so account for that
            this.sessionService.dialogMessage$.pipe(debounce(() => of(this.dialogMessageDebounceTime)))
        ).pipe(
            filter(() => CONFIGURATION.enableKeybinds)
        );

        // Deactivate when the screen type changes (for example, we're a screen and the message is a dialog).
        //
        // The reason we deactivate, instead of unregistering, is that after closing dialogs that are created
        // on the client, like the Kebab menu, there is no SCREEN message from the server to re-register the
        // screen behind the dialog.
        screenMessages$.pipe(
            filter(message => this.didScreenTypeChange(message)),
            filter(() => this.keybindingZoneService.isActive()),
            takeUntil(merge(this.stop$, this.destroyed$))
        ).subscribe(() => this.deactivate());

        // Handle messages for this screen type
        screenMessages$.pipe(
            filter(message => message.type === this.messageType),
            takeUntil(merge(this.stop$, this.destroyed$))
        ).subscribe(message => this.handleScreenMessage(message));

        if (CONFIGURATION.enableKeybinds) {
            console.log(`[KeybindingZoneScreenService]: Listening for "${this.messageType}" messages`);
        }
    }

    stop(): void {
        this.stop$.next();
        console.log('[KeybindingZoneScreenService]: Stopped listening for screen messages');
    }

    handleScreenMessage(message: any): void {
        if (!this.keybindingZoneService.isRegistered()) {
            this.register(message);
        } else if (this.didScreenChange(message)) {
            this.unregister();
            this.register(message);
        } else if (this.keybindingZoneService.isActive()) {
            this.updateRegistration(message);
        } else {
            this.keybindingZoneService.activate();
        }
    }

    getMessageType(): string {
        return this.messageType;
    }

    register(message: any): void {
        const registration: KeybindingZone = {
            id: message.id || `${this.messageType}-no-message-id`,
            actionsObj: message
        };

        console.log(`[KeybindingZoneScreenService]: Registering keybindings "${registration.id}" in message "${message.id}"`);
        this.keybindingZoneService.register(registration);

        this.keybindingZoneService.activate();
        this.messageId = message.id;
    }

    deactivate(): void {
        console.log(`[KeybindingZoneScreenService]: Deactivated keybindings in message "${this.messageId}"`);
        this.keybindingZoneService.deactivate();

    }

    updateRegistration(message: any): void {
        console.log(`[KeybindingZoneScreenService]: Updating keybindings in message "${message.id}"`);
        this.keybindingZoneService.updateZone(message);
    }

    unregister(): void {
        console.log(`[KeybindingZoneScreenService]: Unregistering "${this.keybindingZoneService.getZoneId()}"`);
        this.keybindingZoneService.unregister();
    }

    didScreenChange(message: any): boolean {
        return message.id
            && this.messageId
            && this.messageId !== message.id;
    }

    didScreenTypeChange(message: any): boolean {
        return message.type
            && this.messageType
            && this.messageType !== message.type;
    }

    ngOnDestroy(): void {
        this.unregister();
        this.destroyed$.next();
    }
}
