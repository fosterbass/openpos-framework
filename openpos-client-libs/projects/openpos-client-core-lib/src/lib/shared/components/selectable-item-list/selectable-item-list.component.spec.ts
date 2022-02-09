import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
    KeybindingTestUtils,
    MockActionService,
    MockSessionService
} from '../../../core/keybindings/keybinding-test.utils';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { SessionService } from '../../../core/services/session.service';
import { ActionService } from '../../../core/actions/action.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CONFIGURATION } from '../../../configuration/configuration';
import { IconComponent } from '../icon/icon.component';
import {
    SelectableItemListComponent,
    SelectableItemListComponentConfiguration
} from './selectable-item-list.component';
import { MatIconModule } from '@angular/material/icon';
import { ISelectableListData } from './selectable-list-data.interface';
import { SelectionMode } from '../../../core/interfaces/selection-mode.enum';
import { BehaviorSubject } from 'rxjs';

// This isn't implemented in JSDOM
// https://github.com/jsdom/jsdom/issues/1695
// tslint:disable: only-arrow-functions
if (!HTMLElement.prototype.scrollIntoView) {
    HTMLElement.prototype.scrollIntoView = function() {
    };
}

function createListData(): ISelectableListData<any> {
    const items: { [key: number]: any } = {
        1: {id: '1'},
        2: {id: '2'},
        3: {id: '3'},
        4: {id: '4'},
        5: {id: '5'},
        6: {id: '6'},
        7: {id: '7'},
        8: {id: '8'}
    };
    const disabledItems: { [key: number]: any } = {
        2: {id: '2'}
    };

    return {
        items: new Map(Object.entries(items) as any),
        disabledItems: new Map<number, any>(Object.entries(disabledItems) as any)
    };
}

describe('SelectableItemListComponent', () => {
    let fixture: ComponentFixture<SelectableItemListComponent<any>>;
    let selectableItemList: SelectableItemListComponent<any>;
    let keybindingZoneService: KeybindingZoneService;
    let mockActionService: MockActionService;
    let listData: ISelectableListData<any>;
    let listData$: BehaviorSubject<ISelectableListData<any>>;
    let config: SelectableItemListComponentConfiguration;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                MatIconModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                SelectableItemListComponent,
                IconComponent
            ],
            providers: [
                KeybindingZoneService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        listData = createListData();
        listData$ = new BehaviorSubject(listData);
        mockActionService = TestBed.inject(ActionService) as any;

        fixture = TestBed.createComponent(SelectableItemListComponent);
        keybindingZoneService = fixture.debugElement.injector.get(KeybindingZoneService);
        keybindingZoneService.register('test');
        keybindingZoneService.activate();

        config = new SelectableItemListComponentConfiguration();
        config.numItemsPerPage = 5;
        config.selectionMode = SelectionMode.Single;
        config.defaultSelectItemIndex = 0;
        config.totalNumberOfItems = listData.items.size;

        selectableItemList = fixture.componentInstance;
        selectableItemList.listData = listData$;
        selectableItemList.selectedItem = listData[0];
        selectableItemList.defaultSelect = true;
        selectableItemList.configuration = config;

        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should set items', () => {
        expect(selectableItemList.items).toEqual(listData.items);
    });

    it('should set disabled items', () => {
        expect(selectableItemList.disabledItems).toEqual(listData.disabledItems);
    });

    describe('keybindings', () => {
        it('should select the next item on ArrowDown key', () => {
            KeybindingTestUtils.pressKey('ArrowDown');
            fixture.detectChanges();
            expect(fixture.nativeElement.querySelectorAll('.selection-list')[1].classList).toContain('selected');
        });

        it('should select the previous item on ArrowUp key', () => {
            config.defaultSelectItemIndex = 4;
            selectableItemList.configuration = config;

            KeybindingTestUtils.pressKey('ArrowUp');
            fixture.detectChanges();

            expect(fixture.nativeElement.querySelectorAll('.selection-list')[3].classList).toContain('selected');
        });

        it('should go to the next page on ArrowRight key', () => {
            KeybindingTestUtils.pressKey('ArrowRight');
            fixture.detectChanges();

            // innerText doesn't work in JSDOM
            if (!fixture.nativeElement.querySelector('.current-page-number').innerText) {
                expect(selectableItemList.currentPage).toEqual(2);
            } else {
                expect(fixture.nativeElement.querySelector('.current-page-number').innerText).toEqual('2');
            }
        });

        it('should go to the previous page on ArrowLeft key', () => {
            selectableItemList.currentPage = 2;
            fixture.detectChanges();

            // innerText doesn't work in JSDOM
            if (!fixture.nativeElement.querySelector('.current-page-number').innerText) {
                expect(selectableItemList.currentPage).toEqual(2);
            } else {
                expect(fixture.nativeElement.querySelector('.current-page-number').innerText).toEqual('2');
            }

            KeybindingTestUtils.pressKey('ArrowLeft');
            fixture.detectChanges();

            // innerText doesn't work in JSDOM
            if (!fixture.nativeElement.querySelector('.current-page-number').innerText) {
                expect(selectableItemList.currentPage).toEqual(1);
            } else {
                expect(fixture.nativeElement.querySelector('.current-page-number').innerText).toEqual('1');
            }
        });

        it('should unselect the selected item on Escape key', () => {
            KeybindingTestUtils.pressKey('Escape');
            fixture.detectChanges();
            expect(fixture.nativeElement.querySelector('.selection-list.selected')).toBeFalsy();
        });
    });
});
