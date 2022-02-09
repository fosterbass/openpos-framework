import { TestBed } from '@angular/core/testing';
import { CONFIGURATION } from '../../configuration/configuration';
import { KeybindingZoneService } from './keybinding-zone.service';
import { KeybindingTestUtils, MockActionService } from './keybinding-test.utils';
import { KeybindingZone } from './keybinding-zone.interface';
import { ActionService } from '../actions/action.service';
import { KeybindingService } from './keybinding.service';

describe('KeybindingZoneService', () => {
    let keybindingZoneService: KeybindingZoneService;
    let keybindingService: KeybindingService;
    let mockActionService: MockActionService;
    let subscriptionCallback: any;
    let userZone: KeybindingZone;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                KeybindingZoneService,
                {provide: ActionService, useClass: MockActionService}
            ]
        });

        CONFIGURATION.enableKeybinds = true;

        mockActionService = TestBed.inject(ActionService) as any;
        subscriptionCallback = jasmine.createSpy('Subscription Callback');

        userZone = KeybindingTestUtils.createUserZone(mockActionService);

        keybindingService = TestBed.inject(KeybindingService);
        keybindingZoneService = TestBed.inject(KeybindingZoneService);
        keybindingZoneService.register(userZone);
        keybindingZoneService.activate();
    });

    it('should register with current action service', () => {
        expect(keybindingZoneService.getZone().actionService).toBe(mockActionService as any);
    });

    it('should be locked to the registered zone id', () => {
        expect(keybindingZoneService.getZoneId()).toEqual(userZone.id);
    });

    it('should update the zone using the registered id', () => {
        const doItAction = {
            keybind: 'F3',
            action: 'DoIt'
        };
        const updatedUserZone = {
            doIt: doItAction
        };

        // Don't specify the id to make sure the zone id saved during registration is used
        keybindingZoneService.updateZone(updatedUserZone);

        expect(keybindingService.getZone(userZone.id)).toEqual({
            actionService: mockActionService as any,
            actions: [doItAction],
            actionsObj: updatedUserZone,
            id: userZone.id
        });
    });

    it('should unregister using the registered id', () => {
        keybindingZoneService.unregister();
        expect(keybindingService.isRegistered(userZone.id)).toBeFalse();
    });

    describe('keybinding actions', () => {
        it('should be handled when it is the active zone', () => {
            keybindingZoneService.getKeyDownActionEvent().subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback).toHaveBeenCalledOnceWith({
                domEvent: jasmine.any(Object),
                action: userZone.actionsObj.killUser,
                actionPayload: jasmine.falsy(),
                zone: keybindingZoneService.getZone(),
                didDoAction: true
            });
        });

        it('should not be handled when it is not the active zone', () => {
            keybindingService.register({
                id: 'stupid-zone'
            });
            keybindingService.activate('stupid-zone');

            keybindingZoneService.getKeyDownActionEvent().subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback).not.toHaveBeenCalled();
        });
    });

    describe('keydown events', () => {
        it('should be handled when it is the active zone', () => {
            keybindingZoneService.getKeyDownEvent().subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey('Enter');

            expect(subscriptionCallback).toHaveBeenCalledOnceWith({
                domEvent: jasmine.any(Object),
                action: jasmine.falsy(),
                actionPayload: jasmine.falsy(),
                zone: keybindingZoneService.getZone(),
                didDoAction: false
            });
        });

        it('should not be handled when it is not the active zone', () => {
            keybindingService.register({
                id: 'stupid-zone'
            });
            keybindingService.activate('stupid-zone');

            keybindingZoneService.getKeyDownEvent().subscribe(subscriptionCallback);
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            expect(subscriptionCallback).not.toHaveBeenCalled();
        });

        it('should be handled for specific keys', () => {
            keybindingZoneService.getKeyDownEvent('Enter,Shift+Escape').subscribe(subscriptionCallback);

            // The callback shouldn't be called for this
            KeybindingTestUtils.pressKey(userZone.actionsObj.killUser.event);

            // The callback should be called for each of these
            KeybindingTestUtils.pressKey('Enter');
            KeybindingTestUtils.pressKey({
                key: 'Escape',
                shiftKey: true
            });

            expect(subscriptionCallback).toHaveBeenCalledWith({
                domEvent: jasmine.any(Object),
                action: jasmine.falsy(),
                actionPayload: jasmine.falsy(),
                zone: keybindingZoneService.getZone(),
                didDoAction: false
            });
            expect(subscriptionCallback.calls.count()).toEqual(2);
        });
    });

    describe('disabled', () => {
        beforeEach(() => {
            CONFIGURATION.enableKeybinds = false;
        });

        it('should not register', () => {
            keybindingZoneService.register('nooooooo!!!');
            expect(keybindingZoneService.getZoneId()).toEqual(userZone.id);
        });

        it('should not update', () => {
            const expectedActions = keybindingZoneService.getZone().actions;
            userZone.actionsObj = {
                action: 'No!',
                keybind: 'F10'
            };
            keybindingZoneService.updateZone(userZone);

            expect(keybindingZoneService.getZone().actions).toEqual(expectedActions);
        });
    });
});
