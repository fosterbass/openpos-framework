import { TestBed } from '@angular/core/testing';
import { KeybindingService } from './keybinding.service';
import { CONFIGURATION } from '../../configuration/configuration';
import { KeybindingZone } from './keybinding-zone.interface';
import { KeybindingTestUtils, MockActionService } from './keybinding-test.utils';
import { ActionService } from '../actions/action.service';

describe('KeybindingService', () => {
    let keybindingService: KeybindingService;
    let mockActionService: MockActionService;
    let userZone: KeybindingZone;
    let baconStripZone: KeybindingZone;
    let subscriptionCallback: any;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                {provide: ActionService, useClass: MockActionService}
            ]
        });

        CONFIGURATION.enableKeybinds = true;

        subscriptionCallback = jasmine.createSpy('Subscription Callback');
        mockActionService = TestBed.inject(ActionService) as any;

        userZone = KeybindingTestUtils.createUserZone(mockActionService);
        baconStripZone = KeybindingTestUtils.createBaconStripZone(mockActionService);

        keybindingService = TestBed.inject(KeybindingService);
        keybindingService.register(userZone);
        keybindingService.register(baconStripZone);
        keybindingService.activate(userZone.id);
    });

    it('should not execute when the service is disabled', () => {
        keybindingService.disable();
        // Check the lowest-level observable used by the service because if it doesn't fire nothing else will
        keybindingService.getAllKeyDownEvents().subscribe(subscriptionCallback);
        KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

        expect(subscriptionCallback).not.toHaveBeenCalled();
    });

    it('should support removing keybindings', () => {
        keybindingService.removeKeybinding(userZone.id, userZone.actionsObj.killUser.keybind);
        keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
        KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

        expect(mockActionService.doAction).not.toHaveBeenCalled();
    });

    it('should support removing a list of keybindings', () => {
        const keybindingsToRemove = [
            // Test strings and keybinding objects
            userZone.actionsObj.logout.keybind,
            userZone.actionsObj.killUser,
            {
                keybind: 'Ctrl+Shift+F5',
                action: 'TestRemovingActionThatDoesNotExist'
            }
        ];

        keybindingService.removeAllKeybindings(userZone.id, keybindingsToRemove);
        keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);

        KeybindingTestUtils.pressKey(userZone.actionsObj.logout.event);
        KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);
        // Just to be sure, test the removed keybinding that never existed in the first place
        KeybindingTestUtils.pressKey({
            key: 'F5',
            ctrlKey: true,
            shiftKey: true
        });

        expect(mockActionService.doAction).not.toHaveBeenCalled();
    });

    it('should support removing a list of keybindings from an object', () => {
        const keybindingsToRemoveObj = {
            killUser: userZone.actionsObj.killUser,
            testRemovingActionThatDoesNotExist: {
                keybind: 'Ctrl+Shift+F5',
                action: 'TestRemovingActionThatDoesNotExist'
            }
        };

        keybindingService.removeAllKeybindings(userZone.id, keybindingsToRemoveObj);
        keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);

        KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);
        // Just to be sure, test the removed keybinding that never existed in the first place
        KeybindingTestUtils.pressKey({
            key: 'F5',
            ctrlKey: true,
            shiftKey: true
        });

        expect(mockActionService.doAction).not.toHaveBeenCalled();
    });

    it('should support adding a list of keybindings', () => {
        const keybindingsToAdd = [
            // Test that a duplicate replaces existing
            {
                keybind: 'Ctrl+Cmd+Q',
                action: 'KillUser'
            },
            {
                keybind: 'Enter',
                action: 'DigTunnelToGregsHouse'
            }
        ];
        keybindingService.addAllKeybindings(userZone.id, keybindingsToAdd);
        keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);

        KeybindingTestUtils.pressKey({
            key: 'Q',
            ctrlKey: true,
            metaKey: true
        });
        KeybindingTestUtils.pressKey('Enter');

        expect(subscriptionCallback.calls.count()).toEqual(2);
        expect(mockActionService.doAction.calls.count()).toEqual(2);

        expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeTrue();
        expect(subscriptionCallback.calls.argsFor(1)[0].didDoAction).toBeTrue();

        expect(mockActionService.doAction).toHaveBeenCalledWith(keybindingsToAdd[0], jasmine.falsy());
        expect(mockActionService.doAction).toHaveBeenCalledWith(keybindingsToAdd[1], jasmine.falsy());
    });

    it('should support adding keybindings from an object', () => {
        const keybindingsToAddObj = {
            // Test that a duplicate replaces existing
            killUser: {
                keybind: 'F5',
                action: 'KillUser'
            },
            digTunnelToGregsHouse: {
                keybind: 'Enter',
                action: 'DigTunnelToGregsHouse'
            }
        };
        keybindingService.addAllKeybindings(userZone.id, keybindingsToAddObj);
        keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);

        KeybindingTestUtils.pressKey('F5');
        KeybindingTestUtils.pressKey('Enter');

        expect(subscriptionCallback.calls.count()).toEqual(2);
        expect(mockActionService.doAction.calls.count()).toEqual(2);

        expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeTrue();
        expect(subscriptionCallback.calls.argsFor(1)[0].didDoAction).toBeTrue();

        expect(mockActionService.doAction).toHaveBeenCalledWith(keybindingsToAddObj.killUser, jasmine.falsy());
        expect(mockActionService.doAction).toHaveBeenCalledWith(keybindingsToAddObj.digTunnelToGregsHouse, jasmine.falsy());
    });

    it('should support adding keybindings', () => {
        keybindingService.addKeybinding(userZone.id, userZone.actionsObj.killUser);
        keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
        KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

        expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeTrue();
        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(userZone.actionsObj.killUser, null);
    });

    it('should prevent duplicates when adding keybindings and keep the most recent', () => {
        keybindingService.addKeybinding(userZone.id, userZone.actionsObj.killUser);
        userZone = keybindingService.getZone(userZone.id);
        expect(userZone.actions.filter(action => action.keybind === userZone.actionsObj.killUser.keybind).length).toEqual(1);
    });

    it('should restore previously activated zone', () => {
        keybindingService.activate(baconStripZone.id);
        keybindingService.restorePreviousActivation();
        expect(keybindingService.getActiveZoneId()).toEqual(userZone.id);
    });

    it('should unregister zones', () => {
        // Check for leaks by subscribing before unregistering the zone
        keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);

        keybindingService.unregister(userZone.id);
        KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

        expect(subscriptionCallback).not.toHaveBeenCalled();
        expect(keybindingService.getActiveZoneId()).toBeFalsy();
        expect(keybindingService.getZone(userZone.id)).toBeFalsy();
    });

    describe('keydown events', () => {
        it('should always be handled regardless of the active zone', () => {
            // Make no zones active
            keybindingService.deactivate(keybindingService.getActiveZoneId());

            keybindingService.getAllKeyDownEvents().subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey('F4');

            expect(subscriptionCallback).toHaveBeenCalledOnceWith({
                domEvent: jasmine.any(Object),
                action: jasmine.falsy(),
                actionPayload: jasmine.falsy(),
                zone: jasmine.falsy(),
                didDoAction: false
            });
        });

        it('should be handled for specific keys', () => {
            keybindingService.getAllKeyDownEvents('Enter').subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey('Enter');
            KeybindingTestUtils.pressKey('F4');

            expect(subscriptionCallback).toHaveBeenCalledOnceWith({
                domEvent: jasmine.any(Object),
                action: jasmine.falsy(),
                actionPayload: jasmine.falsy(),
                zone: keybindingService.getZone(keybindingService.getActiveZoneId()),
                didDoAction: false
            });
        });

        it('should be handled for all keys', () => {
            keybindingService.getAllKeyDownEvents().subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey('Enter');
            KeybindingTestUtils.pressKey('F4');

            expect(subscriptionCallback).toHaveBeenCalledWith({
                domEvent: jasmine.any(Object),
                action: jasmine.falsy(),
                actionPayload: jasmine.falsy(),
                zone: keybindingService.getZone(keybindingService.getActiveZoneId()),
                didDoAction: false
            });
            expect(subscriptionCallback.calls.count()).toEqual(2);
        });
    });

    describe('keybinding actions', () => {
        it('should execute in an active zone', () => {
            keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeTrue();
            expect(mockActionService.doAction).toHaveBeenCalledOnceWith(userZone.actionsObj.killUser, null);
        });

        it('should be ignored in an inactive zone', () => {
            keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(baconStripZone.actionsObj.punchCashier.event);
            expect(mockActionService.doAction).not.toHaveBeenCalled();
        });

        it('should not execute when no ActionService is set', () => {
            delete userZone.actionService;

            keybindingService.updateZone(userZone);
            keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeFalse();
            expect(mockActionService.doAction).not.toHaveBeenCalled();
        });

        it('should not execute when an action does not match a pressed key', () => {
            keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey('Enter');
            expect(mockActionService.doAction).not.toHaveBeenCalled();
        });

        it('should only execute callback for filtered keys', () => {
            keybindingService.getKeyDownZoneActionEvent(userZone.id, userZone.actionsObj.killUser.keybind).subscribe(subscriptionCallback);
            keybindingService.getKeyDownZoneActionEvent(userZone.id, 'F12').subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeTrue();
            expect(mockActionService.doAction).toHaveBeenCalledOnceWith(userZone.actionsObj.killUser, jasmine.falsy());
        });

        it('should not execute when ActionService reports disabled', () => {
            mockActionService.updateActionDisabledState(userZone.actionsObj.killUser.action, true);
            keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeFalse();
            expect(mockActionService.doAction).not.toHaveBeenCalled();
        });

        it('should not execute when enabled is false', () => {
            // Remove the ActionService so it's not used to check for enabled actions
            delete userZone.actionService;
            keybindingService.updateZone(userZone);

            userZone.actionsObj.killUser.enabled = false;
            keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeFalse();
            expect(mockActionService.doAction).not.toHaveBeenCalled();
        });

        it('should set the payload', () => {
            const payload = {
                withWeapon: 'Punch With Own Fist'
            };

            keybindingService.getKeyDownZoneActionEvent(userZone.id).subscribe(subscriptionCallback);
            keybindingService.getNeedActionPayload(userZone.id)
                .subscribe(keybindingAction => keybindingAction.payload = payload);

            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(mockActionService.doAction).toHaveBeenCalledOnceWith(userZone.actionsObj.killUser, payload);
            expect(subscriptionCallback).toHaveBeenCalledOnceWith({
                domEvent: jasmine.any(Object),
                action: userZone.actionsObj.killUser,
                zone: keybindingService.getZone(keybindingService.getActiveZoneId()),
                actionPayload: payload,
                didDoAction: true
            });
        });

        it('should support cancelling a pending action', () => {
            keybindingService.getShouldDoAction(userZone.id)
                .subscribe(pendingAction => pendingAction.cancel = true);

            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);
            expect(mockActionService.doAction).not.toHaveBeenCalled();
        });
    });

    describe('disabled', () => {
        beforeEach(() => {
            CONFIGURATION.enableKeybinds = false;
        });

        it('should not execute', () => {
            keybindingService.getAllKeyDownEvents().subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback).not.toHaveBeenCalled();
        });

        it('should not register', () => {
            keybindingService.register('nooooooo!!!');
            expect(keybindingService.isRegistered('nooooooo!!!')).toBeFalse();
        });

        it('should not update', () => {
            const expectedActions = keybindingService.getZone(userZone.id).actions;
            userZone.actionsObj = {
                action: 'No!',
                keybind: 'F10'
            };
            keybindingService.updateZone(userZone);

            expect(keybindingService.getZone(userZone.id).actions).toEqual(expectedActions);
        });
    });
});
