import { MatDialogModule } from '@angular/material/dialog';
import { KeyPressProvider } from '../../providers/keypress.provider';
import { DisabledKeyPressProvider } from '../../providers/disabled-keypress.provider';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
    KeybindingTestUtils,
    MockActionService,
    MockSessionService
} from '../../../core/keybindings/keybinding-test.utils';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { SessionService } from '../../../core/services/session.service';
import { ActionService } from '../../../core/actions/action.service';
import { MatCardModule } from '@angular/material/card';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CONFIGURATION } from '../../../configuration/configuration';
import { ItemCardComponent } from './item-card.component';
import { CurrencyTextComponent } from '../currency-text/currency-text.component';
import { IconComponent } from '../icon/icon.component';
import { KebabLabelButtonComponent } from '../kebab-label-button/kebab-label-button.component';

function createSaleItem(): any {
    return {
        id: '1',
        menuItems: [
            {
                keybind: 'F4',
                action: 'StealItem'
            }, {
                keybind: 'F5',
                action: 'ThrowAtCashier'
            }
        ]
    };
}

describe('ItemCardComponent', () => {
    let fixture: ComponentFixture<ItemCardComponent>;
    let itemCard: ItemCardComponent;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;
    let keybindingZoneService: KeybindingZoneService;
    let saleItem: any;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                MatCardModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                ItemCardComponent,
                CurrencyTextComponent,
                IconComponent,
                KebabLabelButtonComponent
            ],
            providers: [
                KeybindingZoneService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService},
                {provide: KeyPressProvider, useClass: DisabledKeyPressProvider}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        saleItem = createSaleItem();
        mockActionService = TestBed.inject(ActionService) as any;
        mockSessionService = TestBed.inject(SessionService) as any;
        keybindingZoneService = TestBed.inject(KeybindingZoneService);

        keybindingZoneService.register('test');
        keybindingZoneService.activate();

        fixture = TestBed.createComponent(ItemCardComponent);
        itemCard = fixture.componentInstance;
        itemCard.item = saleItem;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    // Ensure the kebab menu is closed after each test
    afterEach(() => fixture.destroy());

    it('should be expanded', () => {
        expect(fixture.nativeElement.querySelector('.left-side').classList).not.toContain('collapsed');
    });

    it('should be collapsed', () => {
        itemCard.expanded = false;
        fixture.detectChanges();
        expect(fixture.nativeElement.querySelector('.left-side.collapsed')).toBeTruthy();
    });

    it('should disable menu buttons for actions that start with "<"', () => {
        const disabledAction = {
            keybind: 'F6',
            action: '<IWillNotRun!!!!'
        };
        saleItem.menuItems.push(disabledAction);
        // Make sure the reference changes to force change detection to see changes
        itemCard.item = {
            ...saleItem,
            menuItems: [{
                keybind: 'F6',
                action: '<IWillNotRun!!!!'
            }]
        };

        KeybindingTestUtils.pressKey(' ');
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('.item-card-button').disabled).toBeTrue();
    });

    describe('keybindings', () => {
        it('should open kebab menu when sale item has multiple actions', () => {
            KeybindingTestUtils.pressKey(' ');
            fixture.detectChanges();
            expect(document.querySelector('app-kebab-menu')).not.toBeFalsy();
        });

        it('should execute the menu action when the sale item has one action', () => {
            saleItem.menuItems.pop();
            // Make sure the reference changes to force change detection to see changes
            itemCard.item = {
                ...saleItem
            };
            KeybindingTestUtils.pressKey(' ');
            fixture.detectChanges();

            expect(document.querySelector('app-kebab-menu')).toBeFalsy();
            expect(mockActionService.doAction).toHaveBeenCalledOnceWith(saleItem.menuItems[0], [jasmine.falsy()]);
        });
    });
});
