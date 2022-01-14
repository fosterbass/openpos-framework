import { AfterViewInit, Component, Injector, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ArrowTabDirective } from './arrow-tab.directive';
import { ArrowTabItemDirective } from './arrow-tab-item.directive';
import { SessionService } from '../../core/services/session.service';
import {
    KeybindingTestUtils,
    MockActionService,
    MockSessionService
} from '../../core/keybindings/keybinding-test.utils';
import { ActionService } from '../../core/actions/action.service';
import { KeybindingService } from '../../core/keybindings/keybinding.service';
import { KeybindingZoneService } from '../../core/keybindings/keybinding-zone.service';
import { KeybindingParserService } from '../../core/keybindings/keybinding-parser.service';
import { MatDialogModule } from '@angular/material/dialog';
import { KeyPressProvider } from '../providers/keypress.provider';
import { DisabledKeyPressProvider } from '../providers/disabled-keypress.provider';
import { CONFIGURATION } from '../../configuration/configuration';

@Component({
    template: `
        <!--
            JSDom doesn't return the focused element without a tabindex
            https://stackoverflow.com/questions/38681827/jsdom-9-1-does-not-set-document-activeelement-when-focusing-a-node
        -->
        <ul appArrowTab>
            <li>
                <a tabindex="0" appArrowTabItem></a>
            </li>
            <li>
                <a tabindex="1" appArrowTabItem></a>
            </li>
            <li>
                <a tabindex="2" appArrowTabItem></a>
            </li>
        </ul>`,
})
class TestHostComponent implements AfterViewInit {
    @ViewChild(ArrowTabDirective)
    public arrowTab: ArrowTabDirective;
    @ViewChildren(ArrowTabItemDirective)
    public arrowTabItems: QueryList<ArrowTabItemDirective>;
    public actionService: MockActionService;

    constructor(public keybindingZoneService: KeybindingZoneService, injector: Injector) {
        this.actionService = injector.get(ActionService) as any;
        this.keybindingZoneService.register('test-component');
        this.keybindingZoneService.activate();
    }

    ngAfterViewInit(): void {
        // I can't figure out why the buttons come back empty in the ArrowTabDirective, so this fixes that
        this.arrowTab.buttons = this.arrowTabItems;
    }
}

describe('ArrowTabDirective', () => {
    let fixture: ComponentFixture<TestHostComponent>;
    let arrowTabDirective: ArrowTabDirective;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            // Not sure why it's necessary to import the MatDialog because it's not used
            imports: [MatDialogModule],
            declarations: [
                ArrowTabDirective,
                ArrowTabItemDirective,
                TestHostComponent
            ],
            providers: [
                KeybindingService,
                KeybindingZoneService,
                KeybindingParserService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService},
                {provide: KeyPressProvider, useClass: DisabledKeyPressProvider}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;
        fixture = TestBed.createComponent(TestHostComponent);
        fixture.detectChanges();
        arrowTabDirective = fixture.componentInstance.arrowTab;
    });

    describe('with no focused item', () => {
        it('should select the first tab item on next', () => {
            KeybindingTestUtils.pressKey('ArrowDown');
            expect(document.activeElement).toBe(fixture.nativeElement.querySelector('li:first-child > a'));
        });

        it('should select the last tab item on previous', () => {
            KeybindingTestUtils.pressKey('ArrowUp');
            expect(document.activeElement).toBe(fixture.nativeElement.querySelector('li:last-child > a'));
        });
    });

    describe('with focused item', () => {
        it('should select the next tab item', () => {
            fixture.nativeElement.querySelector('li:nth-child(1) > a').focus();
            KeybindingTestUtils.pressKey('ArrowDown');
            expect(document.activeElement).toBe(fixture.nativeElement.querySelector('li:nth-child(2) > a'));
        });

        it('should select the previous tab item', () => {
            fixture.nativeElement.querySelector('li:nth-child(2) > a').focus();
            KeybindingTestUtils.pressKey('ArrowUp');
            expect(document.activeElement).toBe(fixture.nativeElement.querySelector('li:nth-child(1) > a'));
        });
    });

    describe('with disabled item', () => {
        it('should skip the next item', () => {
            fixture.nativeElement.querySelector('li:nth-child(1) > a').focus();
            fixture.nativeElement.querySelector('li:nth-child(2) > a').disabled = true;
            KeybindingTestUtils.pressKey('ArrowDown');

            expect(document.activeElement).toBe(fixture.nativeElement.querySelector('li:nth-child(3) > a'));
        });

        it('should skip the previous item', () => {
            fixture.nativeElement.querySelector('li:nth-child(2) > a').disabled = true;
            fixture.nativeElement.querySelector('li:nth-child(3) > a').focus();
            KeybindingTestUtils.pressKey('ArrowUp');

            expect(document.activeElement).toBe(fixture.nativeElement.querySelector('li:nth-child(1) > a'));
        });
    });

    describe('with no items', () => {
        beforeEach(() => {
            fixture.nativeElement.querySelectorAll('li').forEach(element => element.remove());
            fixture.detectChanges();
        });

        it('should not error trying to select next', () => {
            KeybindingTestUtils.pressKey('ArrowDown');
            expect(document.activeElement).toBe(document.body);
        });

        it('should not error trying to select previous', () => {
            KeybindingTestUtils.pressKey('ArrowUp');
            expect(document.activeElement).toBe(document.body);
        });
    });
});
