import { ComponentFixture, fakeAsync, flush, TestBed, waitForAsync } from '@angular/core/testing';
import { OptionsListComponent } from './options-list.component';
import { KeybindingService } from '../../../core/keybindings/keybinding.service';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import {
    KeybindingTestUtils,
    MockActionService,
    MockSessionService
} from '../../../core/keybindings/keybinding-test.utils';
import { SessionService } from '../../../core/services/session.service';
import { ActionService } from '../../../core/actions/action.service';
import { KeybindingParserService } from '../../../core/keybindings/keybinding-parser.service';
import { CONFIGURATION } from '../../../configuration/configuration';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ArrowTabDirective } from '../../directives/arrow-tab.directive';
import { ArrowTabItemDirective } from '../../directives/arrow-tab-item.directive';
import { OptionButtonComponent } from '../../components/option-button/option-button.component';
import { MessageProvider } from '../../providers/message.provider';
import { MessageTypes } from '../../../core/messages/message-types';
import { LifeCycleMessage } from '../../../core/messages/life-cycle-message';
import { LifeCycleEvents } from '../../../core/messages/life-cycle-events.enum';
import { KeybindingZone } from '../../../core/keybindings/keybinding-zone.interface';
import { MatDialogModule } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatInputModule } from '@angular/material/input';
import { IconComponent } from '../../components/icon/icon.component';

describe('OptionsListComponent', () => {
    let fixture: ComponentFixture<OptionsListComponent>;
    let optionsList: OptionsListComponent;
    let keybindingZoneService: KeybindingZoneService;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;
    let saleZone: KeybindingZone;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                MatFormFieldModule,
                MatInputModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                OptionsListComponent,
                OptionButtonComponent,
                ArrowTabDirective,
                ArrowTabItemDirective,
                IconComponent
            ],
            providers: [
                KeybindingService,
                KeybindingZoneService,
                KeybindingParserService,
                MessageProvider,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        mockSessionService = TestBed.inject(SessionService) as any;
        mockActionService = TestBed.inject(ActionService) as any;

        saleZone = KeybindingTestUtils.createSaleZone(mockActionService);
        keybindingZoneService = TestBed.inject(KeybindingZoneService);
        keybindingZoneService.register(saleZone);
        keybindingZoneService.activate();

        TestBed.inject(MessageProvider).setMessageType(MessageTypes.SCREEN);
        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
            id: saleZone.id,
            type: MessageTypes.SCREEN,
            overflowButton: {
                keybind: 'F5',
                action: 'ShowOverflowItemsInAVeryNiceAndCleanAndWonderfulAndSpectacularKebabMenu'
            },
            options: keybindingZoneService.getZone().actions
        } as any));

        fixture = TestBed.createComponent(OptionsListComponent);
        optionsList = fixture.componentInstance;
        optionsList.listSize = 2;

        fixture.detectChanges();
        await fixture.whenStable();
    });

    // Ensure the kebab menu is closed after each test
    afterEach(waitForAsync(() => {
        fixture.destroy();
        fixture.detectChanges();

        // Sometimes it randomly hangs around event after destroying the component
        fixture.whenStable().then(() => {
            const kebabMenuElement = document.querySelector('app-kebab-menu');
            if (kebabMenuElement) {
                kebabMenuElement.remove();
            }
        });
    }));

    it('should run the action for a clicked button', () => {
        fixture.nativeElement.querySelector('app-option-button:nth-child(2)')
            .dispatchEvent(new Event('click'));
        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(saleZone.actionsObj.throwItem, jasmine.falsy());
    });

    it('should update the overflow button', fakeAsync(() => {
        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
            id: saleZone.id,
            type: MessageTypes.SCREEN,
            overflowButton: {
                keybind: 'F10',
                action: 'UpdateTheShowOverflowItemsInAVeryNiceAndCleanAndWonderfulAndSpectacularKebabMenu'
            },
            options: keybindingZoneService.getZone().actions
        } as any));

        KeybindingTestUtils.pressKey('F10');
        flush();
        expect(document.querySelector('app-kebab-menu')).toBeTruthy();
    }));

    it('should close the dialog when destroyed', fakeAsync(() => {
        optionsList.openKebabMenu();
        fixture.destroy();
        fixture.detectChanges();
        flush();

        expect(document.querySelector('app-kebab-menu')).toBeFalsy();
    }));

    describe('keybindings', () => {
        it('should open the kebab menu when the overflow keybinding key is pressed', fakeAsync(() => {
            KeybindingTestUtils.pressKey('F5');
            flush();
            fixture.detectChanges();

            expect(document.querySelector('app-kebab-menu')).toBeTruthy();
        }));

        it('should not error if the overflow button is not in the screen data', fakeAsync(() => {
            mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
                id: saleZone.id,
                type: MessageTypes.SCREEN,
                overflowButton: null,
                options: keybindingZoneService.getZone().actions
            } as any));

            KeybindingTestUtils.pressKey('F5');
            flush();
            fixture.detectChanges();

            expect(document.querySelector('app-kebab-menu')).toBeFalsy();
        }));
    });
});
