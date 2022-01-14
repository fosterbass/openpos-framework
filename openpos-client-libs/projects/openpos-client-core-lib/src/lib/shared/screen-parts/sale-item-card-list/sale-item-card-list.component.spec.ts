import { MatDialogModule } from '@angular/material/dialog';
import { KeyPressProvider } from '../../providers/keypress.provider';
import { DisabledKeyPressProvider } from '../../providers/disabled-keypress.provider';
import { ComponentFixture, fakeAsync, flush, TestBed, waitForAsync } from '@angular/core/testing';
import { SaleItemCardListComponent } from './sale-item-card-list.component';
import {
    KeybindingTestUtils,
    MockActionService,
    MockSessionService
} from '../../../core/keybindings/keybinding-test.utils';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { SessionService } from '../../../core/services/session.service';
import { ActionService } from '../../../core/actions/action.service';
import { ItemCardComponent } from '../../components/item-card/item-card.component';
import { ImageTextPanelComponent } from '../image-text-panel/image-text-panel.component';
import { LifeCycleMessage } from '../../../core/messages/life-cycle-message';
import { LifeCycleEvents } from '../../../core/messages/life-cycle-events.enum';
import { MessageTypes } from '../../../core/messages/message-types';
import { KeybindingZone } from '../../../core/keybindings/keybinding-zone.interface';
import { MessageProvider } from '../../providers/message.provider';
import { UIDataMessage } from '../../../core/messages/ui-data-message';
import { UIDataMessageService } from '../../../core/ui-data-message/ui-data-message.service';
import { MatCardModule } from '@angular/material/card';
import { CurrencyTextComponent } from '../../components/currency-text/currency-text.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CONFIGURATION } from '../../../configuration/configuration';
import { IconComponent } from '../../components/icon/icon.component';

// This isn't implemented in JSDOM
// https://github.com/jsdom/jsdom/issues/1695
// tslint:disable: only-arrow-functions
HTMLElement.prototype.scrollIntoView = function() {};

function createSaleItems(): any[] {
    return [
        {
            id: '1',
            menuItems: []
        }, {
            id: '2',
            menuItems: []
        }, {
            id: '3',
            menuItems: []
        }
    ];
}

function expectExpandedAt(fixture: ComponentFixture<SaleItemCardListComponent>, expandedIndex: number): void {
    fixture.nativeElement.querySelectorAll('app-item-card .left-side')
        .forEach((element, index) => {
            if (index === expandedIndex) {
                expect(element.classList).not.toContain('collapsed');
            } else {
                expect(element.classList).toContain('collapsed');
            }
        });
}

describe('SaleItemCardListComponent', () => {
    let fixture: ComponentFixture<SaleItemCardListComponent>;
    let saleItemCardList: SaleItemCardListComponent;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;
    let keybindingZoneService: KeybindingZoneService;
    let saleZone: KeybindingZone;
    let itemsChangedCallback: any;
    let saleItemsData: any;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                MatCardModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                SaleItemCardListComponent,
                ItemCardComponent,
                ImageTextPanelComponent,
                CurrencyTextComponent,
                IconComponent
            ],
            providers: [
                KeybindingZoneService,
                UIDataMessageService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService},
                {provide: KeyPressProvider, useClass: DisabledKeyPressProvider}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;
        TestBed.inject(MessageProvider).setMessageType(MessageTypes.SCREEN);

        mockActionService = TestBed.inject(ActionService) as any;
        mockSessionService = TestBed.inject(SessionService) as any;
        keybindingZoneService = TestBed.inject(KeybindingZoneService);
        itemsChangedCallback = jasmine.createSpy('itemsChanged Callback');
        saleItemsData = createSaleItems();

        saleZone = KeybindingTestUtils.createSaleZone(mockActionService);
        keybindingZoneService.register('test');
        keybindingZoneService.activate();

        fixture = TestBed.createComponent(SaleItemCardListComponent);
        saleItemCardList = fixture.componentInstance;
        saleItemCardList.itemsChanged.subscribe(itemsChangedCallback);

        mockSessionService.dispatchMessage(
            new UIDataMessage('sale-items', 1, saleItemsData)
        );

        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
            id: saleZone.id,
            type: MessageTypes.SCREEN,
            providerKey: 'sale-items',
            enableCollapsibleItems: true,
            actions: keybindingZoneService.getZone().actions
        } as any));

        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should raise itemsChanged event after receiving new sale items', () => {
        expect(itemsChangedCallback).toHaveBeenCalledOnceWith(saleItemsData);
    });

    it('should expand the last item when sale items change', () => {
        expect(saleItemCardList.expandedIndex).toEqual(saleItemsData.length - 1);
    });

    describe('keybindings', () => {
        it('should expand the next item on ArrowDown key', fakeAsync(() => {
            saleItemCardList.expandedIndex = 0;
            fixture.detectChanges();

            KeybindingTestUtils.pressKey('ArrowDown');
            fixture.detectChanges();

            expectExpandedAt(fixture, 1);
        }));

        it('should expand the next item on Tab key', () => {
            saleItemCardList.expandedIndex = 0;
            fixture.detectChanges();

            KeybindingTestUtils.pressKey('Tab');
            fixture.detectChanges();

            expectExpandedAt(fixture, 1);
        });

        it('should expand the next item on ArrowUp key', () => {
            KeybindingTestUtils.pressKey('ArrowUp');
            fixture.detectChanges();

            KeybindingTestUtils.pressKey('ArrowUp');
            fixture.detectChanges();

            expectExpandedAt(fixture, 0);
        });
    });
});
