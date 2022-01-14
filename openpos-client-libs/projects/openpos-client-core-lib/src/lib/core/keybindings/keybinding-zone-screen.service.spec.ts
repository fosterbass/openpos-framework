import { TestBed } from '@angular/core/testing';
import { CONFIGURATION } from '../../configuration/configuration';
import { KeybindingZoneService } from './keybinding-zone.service';
import { KeybindingTestUtils, MockActionService, MockSessionService } from './keybinding-test.utils';
import { KeybindingZone } from './keybinding-zone.interface';
import { ActionService } from '../actions/action.service';
import { SessionService } from '../services/session.service';
import { KeybindingZoneScreenService } from './keybinding-zone-screen.service';
import { MessageTypes } from '../messages/message-types';

describe('KeybindingZoneScreenService', () => {
    let keybindingZoneScreenService: KeybindingZoneScreenService;
    let keybindingZoneService: KeybindingZoneService;
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

    describe('when activated', () => {
        beforeEach(() => {
            mockSessionService.dispatchMessage({
                ...userZone,
                type: MessageTypes.SCREEN
            });
            keybindingZoneScreenService.start(MessageTypes.SCREEN);
        });

        it('should deactivate when the screen changes message types', () => {
            mockSessionService.dispatchMessage({
                id: 'dumb-dialog',
                type: MessageTypes.DIALOG
            });
            expect(keybindingZoneService.isActive()).toBeFalse();
            expect(keybindingZoneService.isRegistered()).toBeTrue();
        });

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
});
