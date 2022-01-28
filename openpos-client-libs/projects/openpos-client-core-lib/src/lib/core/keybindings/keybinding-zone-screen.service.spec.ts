import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { CONFIGURATION } from '../../configuration/configuration';
import { KeybindingZoneService } from './keybinding-zone.service';
import { KeybindingTestUtils, MockActionService, MockSessionService } from './keybinding-test.utils';
import { KeybindingZone } from './keybinding-zone.interface';
import { ActionService } from '../actions/action.service';
import { SessionService } from '../services/session.service';
import { KeybindingZoneScreenService } from './keybinding-zone-screen.service';
import { MessageTypes } from '../messages/message-types';
import { KeybindingService } from './keybinding.service';

describe('KeybindingZoneScreenService', () => {
    let keybindingZoneScreenService: KeybindingZoneScreenService;
    let keybindingZoneService: KeybindingZoneService;
    let keybindingService: KeybindingService;
    let mockActionService: ActionService;
    let mockSessionService: MockSessionService;
    let subscriptionCallback: any;
    let userZone: KeybindingZone;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                KeybindingZoneScreenService,
                KeybindingZoneService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService}
            ]
        });

        CONFIGURATION.enableKeybinds = true;

        mockActionService = TestBed.inject(ActionService) as any;
        mockSessionService = TestBed.inject(SessionService) as any;
        subscriptionCallback = jasmine.createSpy('Subscription Callback');

        userZone = KeybindingTestUtils.createUserZone(mockActionService);

        keybindingService = TestBed.inject(KeybindingService);
        keybindingZoneService = TestBed.inject(KeybindingZoneService);
        keybindingZoneScreenService = TestBed.inject(KeybindingZoneScreenService);
    });

    it('should register and activate', () => {
        // Send the message before starting the service to make sure late subscriptions work
        mockSessionService.dispatchMessage({
            ...userZone,
            type: MessageTypes.SCREEN
        });
        keybindingZoneScreenService.start(MessageTypes.SCREEN);

        expect(keybindingZoneService.isActive()).toBeTrue();
    });

    describe('zone activated', () => {
        beforeEach(() => {
            mockSessionService.dispatchMessage({
                ...userZone,
                type: MessageTypes.SCREEN
            });
            keybindingZoneScreenService.start(MessageTypes.SCREEN);
        });

        it('should deactivate when the screen changes message types', fakeAsync(() => {
            mockSessionService.dispatchMessage({
                id: 'dumb-dialog',
                type: MessageTypes.DIALOG
            });
            tick(keybindingZoneScreenService.dialogMessageDebounceTime);

            expect(keybindingZoneService.isActive()).toBeFalse();
            expect(keybindingZoneService.isRegistered()).toBeTrue();
        }));

        it('should activate a new screen if the message type is the same', () => {
            mockSessionService.dispatchMessage({
                id: 'stupid-new-screen',
                type: MessageTypes.SCREEN
            });
            expect(keybindingZoneService.isActive()).toBeTrue();
            expect(keybindingZoneService.getZoneId()).toEqual('stupid-new-screen');
        });

        it('should update the registration', () => {
            const idiotAction = {
                keybind: 'F8',
                action: 'DoBookLearning'
            };

            mockSessionService.dispatchMessage({
                id: userZone.id,
                type: MessageTypes.SCREEN,
                idiotAction
            });
            expect(keybindingZoneService.getZone().actions.includes(idiotAction)).toBeTrue();
        });

        it('should reactivate if already registered', () => {
            // Change screen types to deactivate the current screen
            mockSessionService.dispatchMessage({
                id: 'stupid-new-screen-of-different-type',
                type: MessageTypes.DIALOG
            });

            // Screen should be reactivated because it was already registered
            mockSessionService.dispatchMessage({
                id: userZone.id,
                type: MessageTypes.SCREEN
            });

            expect(keybindingZoneService.getZoneId()).toEqual(userZone.id);
            expect(keybindingZoneService.isActive()).toBeTrue();
        });
    });

    describe('keybindings disabled', () => {
        beforeEach(() => {
            CONFIGURATION.enableKeybinds = false;
        });

        it('should not register session messages', () => {
            mockSessionService.dispatchMessage({
                ...userZone,
                type: MessageTypes.SCREEN
            });
            keybindingZoneScreenService.start(MessageTypes.SCREEN);

            expect(keybindingZoneService.isRegistered()).toBeFalse();
            expect(keybindingZoneService.isActive()).toBeFalse();
        });

        it('should not deactivate', fakeAsync(() => {
            // Temporarily enable key bindings to set an initial message type of SCREEN
            CONFIGURATION.enableKeybinds = true;
            mockSessionService.dispatchMessage({
                ...userZone,
                type: MessageTypes.SCREEN
            });
            keybindingZoneScreenService.start(MessageTypes.SCREEN);

            // Ensure this new message is never registered
            CONFIGURATION.enableKeybinds = false;
            mockSessionService.dispatchMessage({
                id: 'dumb-dialog',
                type: MessageTypes.DIALOG
            });
            tick(keybindingZoneScreenService.dialogMessageDebounceTime);

            // Verify the current zone is not deactivated
            expect(keybindingZoneService.getZoneId()).toEqual(userZone.id);
            expect(keybindingZoneService.isRegistered()).toBeTrue();
            expect(keybindingZoneService.isActive()).toBeTrue();
        }));
    });
});
