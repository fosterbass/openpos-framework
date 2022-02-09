import { KeybindingLockScreenService } from './keybinding-lock-screen.service';
import { TestBed } from '@angular/core/testing';
import { SessionService } from '../services/session.service';
import { KeybindingTestUtils, MockSessionService } from './keybinding-test.utils';
import { KeybindingService } from './keybinding.service';
import { MessageTypes } from '../messages/message-types';
import { CONFIGURATION } from '../../configuration/configuration';

describe('KeybindingLockScreenService', () => {
    let keybindingLockScreenService: KeybindingLockScreenService;
    let keybindingService: KeybindingService;
    let mockSessionService: MockSessionService;
    let subscriptionCallback: any;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                {provide: SessionService, useClass: MockSessionService}
            ]
        });

        CONFIGURATION.enableKeybinds = true;

        mockSessionService = TestBed.inject(SessionService) as any;
        subscriptionCallback = jasmine.createSpy('Subscription Callback');

        keybindingLockScreenService = TestBed.inject(KeybindingLockScreenService);
        keybindingService = TestBed.inject(KeybindingService);
    });

    // Just to improve the code-coverage report from around 7% to at least 90%
    afterEach(() => keybindingLockScreenService.ngOnDestroy());

    it('should disable keybindings when the screen is locked', () => {
        keybindingService.getAllKeyDownEvents().subscribe(subscriptionCallback);

        mockSessionService.dispatchMessage({
            id: 'lock-screen',
            type: MessageTypes.LOCK_SCREEN
        });

        KeybindingTestUtils.pressKey('Enter');
        expect(subscriptionCallback).not.toHaveBeenCalled();
    });

    it('should enable keybindings when the screen is unlocked', () => {
        keybindingService.getAllKeyDownEvents().subscribe(subscriptionCallback);

        // Lock and then unlock the screen
        mockSessionService.dispatchMessage({
            id: 'lock-screen',
            type: MessageTypes.LOCK_SCREEN
        });
        mockSessionService.dispatchMessage({
            id: 'lock-screen',
            type: MessageTypes.UNLOCK_SCREEN
        });

        KeybindingTestUtils.pressKey('Enter');
        expect(subscriptionCallback).toHaveBeenCalled();
    });

    describe('keybindings disabled', () => {
        beforeEach(() => {
            CONFIGURATION.enableKeybinds = false;
        });

        it('should not listen for messages', () => {
            mockSessionService.dispatchMessage({
                id: 'lock-screen',
                type: MessageTypes.LOCK_SCREEN
            });

            // It should not have disabled the keybinding service
            expect(keybindingService.isEnabled()).toBeTrue();
        });
    });
});
