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

    it('should not execute when disabled in app configuration', () => {
        CONFIGURATION.enableKeybinds = false;
        keybindingService.getAllKeyDownEvents().subscribe(subscriptionCallback);
        KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

        expect(subscriptionCallback).not.toHaveBeenCalled();
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

        expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeFalse();
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

            expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeFalse();
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

            expect(subscriptionCallback.calls.argsFor(0)[0].didDoAction).toBeFalse();
            expect(mockActionService.doAction).not.toHaveBeenCalled();
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

            keybindingService.getNeedActionPayload(userZone.id)
                .subscribe(keybindingAction => keybindingAction.payload = payload);

            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);
            expect(mockActionService.doAction).toHaveBeenCalledOnceWith(userZone.actionsObj.killUser, payload);
        });
    });
});
