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
        this.keybindingZoneService.register('test-component');
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

    it('should keybinding and not use the previous value', () => {
        const newAction = {
            keybind: 'F10',
            action: 'TakeThatGreg!'
        };

        fixture.componentInstance.actionItem = newAction;
        fixture.detectChanges();
        KeybindingTestUtils.pressKey('F10');

        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(newAction, jasmine.falsy());
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
});
