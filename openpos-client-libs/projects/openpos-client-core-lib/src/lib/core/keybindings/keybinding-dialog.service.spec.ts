import { TestBed } from '@angular/core/testing';
import { KeybindingService } from './keybinding.service';
import { CONFIGURATION } from '../../configuration/configuration';
import { KeybindingZone } from './keybinding-zone.interface';
import { KeybindingTestUtils, MockSessionService } from './keybinding-test.utils';
import { KeybindingDialogService } from './keybinding-dialog.service';
import { SessionService } from '../services/session.service';
import { LifeCycleMessage } from '../messages/life-cycle-message';
import { LifeCycleEvents } from '../messages/life-cycle-events.enum';
import { ActionMessage } from '../messages/action-message';
import { MessageTypes } from 'openpos-client-core-lib';

describe('KeybindingDialogService', () => {
    let keybindingDialogService: KeybindingDialogService;
    let keybindingService: KeybindingService;
    let mockSessionService: MockSessionService;
    let userZone: KeybindingZone;
    let closeActionMessage: ActionMessage;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                {provide: SessionService, useClass: MockSessionService}
            ]
        });

        CONFIGURATION.enableKeybinds = true;

        keybindingDialogService = TestBed.inject(KeybindingDialogService);
        keybindingService = TestBed.inject(KeybindingService);
        mockSessionService = TestBed.inject(SessionService) as any;

        userZone = KeybindingTestUtils.createUserZone();
        closeActionMessage = new ActionMessage(
            keybindingDialogService.dialogCloseAction,
            keybindingDialogService.doNotBlockForResponse
        );

        keybindingService.register(userZone);
        keybindingService.activate(userZone.id);

        // Start with an open dialog
        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.DialogOpening, null));
    });

    it('should not handle key down events when disabled', () => {
        keybindingDialogService.disable();
        KeybindingTestUtils.pressKey('Escape');
        // Also test keys that don't match what the service is listening for
        KeybindingTestUtils.pressKey('Enter');

        expect(mockSessionService.sendMessage).not.toHaveBeenCalled();
    });

    describe('open dialog', () => {
        it('should not close when there is an existing close action', () => {
            userZone.actionsObj.closeDialog = {
                action: 'Close',
                keybind: 'Escape'
            };
            keybindingService.updateZone(userZone);

            KeybindingTestUtils.pressKey('Escape');
            expect(mockSessionService.sendMessage).not.toHaveBeenCalled();
        });

        it('should close for a matching key when the dialog is closeable', () => {
            mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.DialogOpening, {
                screenType: 'whatever',
                type: MessageTypes.DIALOG,
                id: 'testDialogId',
                dialogProperties: {
                    closeable: true
                }} as any
            ));

            KeybindingTestUtils.pressKey('Escape');
            expect(mockSessionService.sendMessage).toHaveBeenCalledOnceWith(closeActionMessage);
        });

        it('should not close for a matching key when the dialog is not closeable', () => {
            mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.DialogOpening, {
                screenType: 'whatever',
                type: MessageTypes.DIALOG,
                id: 'testDialogId',
                dialogProperties: {
                    closeable: false
                }} as any
            ));

            KeybindingTestUtils.pressKey('Escape');
            expect(mockSessionService.sendMessage).not.toHaveBeenCalled();
        });

        it('should not close when key does not match', () => {
            KeybindingTestUtils.pressKey('Enter');
            expect(mockSessionService.sendMessage).not.toHaveBeenCalled();
        });

        it('should only check for an existing close action in the active zone', () => {
            const gregsZone = {
                id: '',
                actionsObj: {
                    closeGregsBankAccounts: {
                        action: 'CloseThemAllAndGiveToAndyGrandPooBaugh!',
                        keybind: 'Escape'
                    }
                }
            };

            // Register this important zone but don't activate so we make sure it's not being checked
            keybindingService.register(gregsZone);

            // The dialog should be closed because the Escape keybinding is in a non-active zone
            KeybindingTestUtils.pressKey('Escape');
            expect(mockSessionService.sendMessage).toHaveBeenCalledOnceWith(closeActionMessage);
        });
    });

    describe('closed dialog', () => {
        beforeEach(() => {
            mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.DialogClosing, null));
        });

        it('should ignore key down events', () => {
            KeybindingTestUtils.pressKey('Escape');
            // Also test keys that don't match what the service is listening for
            KeybindingTestUtils.pressKey('Enter');
            expect(mockSessionService.sendMessage).not.toHaveBeenCalled();
        });
    });

    describe('keybindings disabled', () => {
        beforeEach(() => {
            CONFIGURATION.enableKeybinds = false;
        });

        it('should not close', () => {
            KeybindingTestUtils.pressKey('Escape');
            expect(mockSessionService.sendMessage).not.toHaveBeenCalled();
        });
    });
});
