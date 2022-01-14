import { ComponentFixture, fakeAsync, flush, TestBed, waitForAsync } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CONFIGURATION } from '../../../configuration/configuration';
import { ActionService } from '../../../core/actions/action.service';
import { KeybindingParserService } from '../../../core/keybindings/keybinding-parser.service';
import {
    KeybindingTestUtils,
    MockActionService,
    MockSessionService
} from '../../../core/keybindings/keybinding-test.utils';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { KeybindingService } from '../../../core/keybindings/keybinding.service';
import { SessionService } from '../../../core/services/session.service';
import { DisabledKeyPressProvider } from '../../providers/disabled-keypress.provider';
import { KeyPressProvider } from '../../providers/keypress.provider';
import { BaconStripComponent } from './bacon-strip.component';
import { MessageProvider } from '../../providers/message.provider';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { IconButtonComponent } from '../../components/icon-button/icon-button.component';
import { MatSidenavModule } from '@angular/material/sidenav';
import { IconComponent } from '../../components/icon/icon.component';
import { BaconDrawerComponent } from './bacon-drawer/bacon-drawer.component';
import { KeybindingZone } from '../../../core/keybindings/keybinding-zone.interface';
import { MessageTypes } from '../../../core/messages/message-types';
import { LifeCycleMessage } from '../../../core/messages/life-cycle-message';
import { LifeCycleEvents } from '../../../core/messages/life-cycle-events.enum';

function getToggleButton(fixture: ComponentFixture<BaconStripComponent>): HTMLElement {
    return fixture.nativeElement.querySelector('.leftside app-icon-button .icon-button');
}

function clickDrawerToggle(fixture: ComponentFixture<BaconStripComponent>) {
    getToggleButton(fixture).dispatchEvent(new Event('click'));
    flush();
    fixture.detectChanges();
}

describe('BaconStripComponent', () => {
    let fixture: ComponentFixture<BaconStripComponent>;
    let baconStrip: BaconStripComponent;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;
    let keybindingZoneService: KeybindingZoneService;
    let saleZone: KeybindingZone;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                MatSidenavModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                IconComponent,
                IconButtonComponent,
                BaconStripComponent,
                BaconDrawerComponent
            ],
            providers: [
                KeybindingService,
                KeybindingZoneService,
                KeybindingParserService,
                MessageProvider,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService},
                {provide: KeyPressProvider, useClass: DisabledKeyPressProvider}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        mockSessionService = TestBed.inject(SessionService) as any;
        mockActionService = TestBed.inject(ActionService) as any;
        TestBed.inject(MessageProvider).setMessageType(MessageTypes.SCREEN);

        saleZone = KeybindingTestUtils.createSaleZone(mockActionService);
        keybindingZoneService = TestBed.inject(KeybindingZoneService);
        keybindingZoneService.register(saleZone);
        keybindingZoneService.activate();

        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
            id: saleZone.id,
            type: MessageTypes.SCREEN,
            // Toggle button only shows if an icon is set
            iconButtonName: 'Hamburger',
            actions: keybindingZoneService.getZone().actions
        } as any));

        fixture = TestBed.createComponent(BaconStripComponent);
        baconStrip = fixture.componentInstance;

        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should automatically execute a single action', fakeAsync(() => {
        const onlyAction = {
            keybind: 'F5',
            action: 'ImSoLonely'
        };

        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
            id: saleZone.id,
            type: MessageTypes.SCREEN,
            // Toggle button only shows if an icon is set
            iconButtonName: 'Hamburger',
            actions: [onlyAction]
        } as any));

        clickDrawerToggle(fixture);
        expect(mockActionService.doAction).toHaveBeenCalledOnceWith(onlyAction, jasmine.falsy());
    }));

    describe('keybindings', () => {
        it('should activate bacon drawer keybindings on open', fakeAsync(() => {
            clickDrawerToggle(fixture);
            expect(baconStrip.baconDrawerComponent.keybindingZoneService.isActive()).toBeTrue();
        }));

        it('should activate previous keybindings on close', fakeAsync(() => {
            clickDrawerToggle(fixture);
            expect(baconStrip.baconDrawerComponent.keybindingZoneService.isActive()).toBeTrue();
            clickDrawerToggle(fixture);
            expect(keybindingZoneService.isActive()).toBeTrue();
        }));

        it('should remove Escape keybinding', () => {
            expect(keybindingZoneService.findActionByKey('Escape')).toBeFalsy();
        });

        it('should open when pressing Escape key', fakeAsync(() => {
            KeybindingTestUtils.pressKey('Escape');
            flush();
            fixture.detectChanges();
            expect(baconStrip.sidenavOpened).toBeTrue();
        }));

        // I'm not sure why the only way I can get this test to work correctly is
        // to use waitForAsync.
        //
        // If I use fakeAsync, like the rest of the tests, this test will not actually
        // flush the last keypress until after the text exits.
        it('should do Escape keybind action before closing', waitForAsync(() => {
            const teleportBackAction = {
                keybind: 'Escape',
                action: 'TeleportBack'
            };
            baconStrip.baconDrawerComponent.keybindingZoneService.updateZone({
                teleportBackAction
            });

            // Open the drawer
            getToggleButton(fixture).dispatchEvent(new Event('click'));
            fixture.detectChanges();
            expect(baconStrip.sidenavOpened).toBeTrue();

            // Press Escape to toggle closed and run drawer action
            KeybindingTestUtils.pressKey('Escape');
            fixture.detectChanges();

            expect(mockActionService.doAction).toHaveBeenCalledOnceWith(teleportBackAction, jasmine.falsy());
            expect(baconStrip.sidenavOpened).toBeFalse();
        }));
    });
});
