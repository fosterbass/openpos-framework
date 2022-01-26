import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
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
import { KebabMenuComponent } from './kebab-menu.component';
import { OptionButtonComponent } from '../option-button/option-button.component';
import { MatCardModule } from '@angular/material/card';
import { KeybindingService } from '../../../core/keybindings/keybinding.service';

function createDialogData(): any {
    return {
        menuItems: [{
            keybind: 'F1',
            action: 'DigTunnelToGregsHouse',
            enabled: true
        }, {
            keybind: 'F2',
            action: 'TakeGregsMoney',
            enabled: true
        }, {
            keybind: 'F3',
            action: 'FireGreg!',
            enabled: true
        }]
    };
}

describe('KebabMenuComponent', () => {
    let fixture: ComponentFixture<KebabMenuComponent>;
    let kebabMenu: KebabMenuComponent;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;
    let keybindingService: KeybindingService;
    let keybindingZoneService: KeybindingZoneService;
    let mockDialog: any;
    let data: any;

    beforeEach(async () => {
        mockDialog = jasmine.createSpyObj('MatDialogRef', ['close']);
        data = createDialogData();

        await TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                MatCardModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                KebabMenuComponent,
                OptionButtonComponent,
                IconComponent
            ],
            providers: [
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService},
                {provide: MAT_DIALOG_DATA, useValue: data},
                {provide: MatDialogRef, useValue: mockDialog}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        mockActionService = TestBed.inject(ActionService) as any;
        mockSessionService = TestBed.inject(SessionService) as any;
        keybindingService = TestBed.inject(KeybindingService);

        fixture = TestBed.createComponent(KebabMenuComponent);
        keybindingZoneService = fixture.debugElement.injector.get(KeybindingZoneService);
        kebabMenu = fixture.componentInstance;
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

    describe('keybindings', () => {
        it('should register the actions in the data', () => {
            expect(keybindingZoneService.getZone().actions).toEqual(data.menuItems);
        });

        it('should activate the zone', () => {
            expect(keybindingZoneService.isActive()).toBeTrue();
        });

        it('should close the dialog with the action for the pressed keybinding', () => {
            KeybindingTestUtils.pressKey('F1');
            fixture.detectChanges();
            expect(mockDialog.close).toHaveBeenCalledOnceWith(data.menuItems[0]);
        });

        it('should deactivate the zone after closing', () => {
            KeybindingTestUtils.pressKey('F2');
            fixture.detectChanges();
            expect(keybindingZoneService.isActive()).toBeFalse();
        });

        it('should unregister zone when destroyed', () => {
            fixture.destroy();
            expect(keybindingZoneService.isRegistered()).toBeFalse();
        });

        it('should restore previously active zone when destroyed', () => {
            keybindingService.register({
                id: 'no-greg-zone'
            });
            keybindingService.activate('no-greg-zone');

            // Reactivate the kebab menu zone
            keybindingZoneService.activate();
            fixture.destroy();
            expect(keybindingService.getActiveZoneId()).toEqual('no-greg-zone');
        });
    });
});
