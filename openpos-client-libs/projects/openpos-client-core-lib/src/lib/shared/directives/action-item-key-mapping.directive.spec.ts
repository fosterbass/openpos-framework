import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SessionService } from '../../core/services/session.service';
import {
    KeybindingTestUtils,
    MockActionService,
    MockSessionService
} from '../../core/keybindings/keybinding-test.utils';
import { ActionService } from '../../core/actions/action.service';
import { KeybindingZoneService } from '../../core/keybindings/keybinding-zone.service';
import { KeybindingParserService } from '../../core/keybindings/keybinding-parser.service';
import { CONFIGURATION } from '../../configuration/configuration';
import { IActionItem } from '../../core/actions/action-item.interface';
import { ActionItemKeyMappingDirective } from './action-item-key-mapping.directive';
import { KeybindingService } from '../../core/keybindings/keybinding.service';

@Component({
    template: `
        <button [actionItem]="actionItem" [actionItemPayload]="payload"></button>
    `
})
class TestHostComponent {
    public actionItem: IActionItem;
    public payload: any;

    constructor(public keybindingZoneService: KeybindingZoneService) {
        this.keybindingZoneService.register('test');
        this.keybindingZoneService.activate();
    }
}

describe('ActionItemKeyMappingDirective', () => {
    let fixture: ComponentFixture<TestHostComponent>;
    let keybindingZoneService: KeybindingZoneService;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;
    let action: IActionItem;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                ActionItemKeyMappingDirective,
                TestHostComponent
            ],
            providers: [
                KeybindingService,
                KeybindingZoneService,
                KeybindingParserService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        mockSessionService = TestBed.inject(SessionService) as any;
        mockActionService = TestBed.inject(ActionService) as any;
        keybindingZoneService = TestBed.inject(KeybindingZoneService);

        action = {
            keybind: 'F3',
            action: 'DoIt!'
        };

        fixture = TestBed.createComponent(TestHostComponent);
        fixture.componentInstance.actionItem = action;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should execute the action for matching keybinding key', () => {
        KeybindingTestUtils.pressKey('F3');
        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(action, jasmine.falsy());
    });

    it('should update keybinding and not use the previous value', () => {
        const newAction = {
            keybind: 'F10',
            action: 'TakeThatGreg!'
        };

        fixture.componentInstance.actionItem = newAction;
        fixture.detectChanges();
        KeybindingTestUtils.pressKey('F10');

        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(newAction, jasmine.falsy());
        // Verify the previous value was removed
        expect(keybindingZoneService.findActionByKey(action.keybind)).toBeFalsy();
    });

    it('should not add undefined action item', () => {
        fixture.componentInstance.actionItem = undefined;
        fixture.detectChanges();
        KeybindingTestUtils.pressKey('F10');

        expect(mockActionService.doAction).not.toHaveBeenCalled();
    });

    it('should set the the action payload for matching keybinding key', () => {
        const payload = 'Andy Takes Greg\'s Money!';

        fixture.componentInstance.payload = payload;
        fixture.detectChanges();
        KeybindingTestUtils.pressKey('F3');

        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(action, payload);
    });

    it('should not add a keybinding if it exists', () => {
        const payload = 'Andy Takes Greg\'s Money!';
        const expectedAction = {
            keybind: 'F10',
            action: 'TransferGregsMoneyToAndy!'
        };

        // Set the existing action to make sure it's not replaced by this directive
        keybindingZoneService.addKeybinding(expectedAction);

        fixture.componentInstance.actionItem = {
            keybind: 'F10',
            action: 'DoNotAddGreg!!!!!'
        };

        // Verify the payload isn't set because we'll trigger a keybinding that doesn't belong to this directive
        fixture.componentInstance.payload = payload;
        fixture.detectChanges();
        KeybindingTestUtils.pressKey('F10');

        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(expectedAction, null);
    });

    it('should not set the payload for an action that does not belong to the directive', () => {
        const takeGregsMoney = {
            keybind: 'Enter',
            action: 'TakeEveryBitOfGregsMoney!!!!'
        };

        // This action is not part of the component
        keybindingZoneService.addKeybinding(takeGregsMoney);
        KeybindingTestUtils.pressKey(takeGregsMoney.keybind);
        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(takeGregsMoney, jasmine.falsy());
    });
});
