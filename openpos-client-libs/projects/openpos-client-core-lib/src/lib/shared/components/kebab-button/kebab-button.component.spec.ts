import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Component, ViewChild } from '@angular/core';
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
import { KebabButtonComponent } from './kebab-button.component';
import { MatDialogModule } from '@angular/material/dialog';

@Component({
    template: `
        <app-kebab-button [keyBinding]="keyBinding"></app-kebab-button>
    `
})
class TestHostComponent {
    @ViewChild(KebabButtonComponent)
    public kebabButton: KebabButtonComponent;
    public keyBinding: string;

    constructor(public keybindingZoneService: KeybindingZoneService) {
        this.keybindingZoneService.register('test-component');
        this.keybindingZoneService.activate();
    }
}

describe('KebabButtonComponent', () => {
    let fixture: ComponentFixture<TestHostComponent>;
    let kebabButton: KebabButtonComponent;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                TestHostComponent,
                KebabButtonComponent,
                IconComponent
            ],
            providers: [
                KeybindingZoneService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;
        fixture = TestBed.createComponent(TestHostComponent);

        fixture.detectChanges();
        kebabButton = fixture.componentInstance.kebabButton;
        kebabButton.keyBinding = 'Ctrl+Enter';

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
        it('should open the kebab menu on the set keybinding', () => {
            KeybindingTestUtils.pressKey({
                key: 'Enter',
                ctrlKey: true
            });
            expect(document.querySelector('app-kebab-menu')).toBeTruthy();
        });

        it('should not open the kebab menu for old keybinding value when new value is set', () => {
            fixture.componentInstance.keyBinding = 'F4';
            fixture.detectChanges();
            KeybindingTestUtils.pressKey({
                key: 'Enter',
                ctrlKey: true
            });

            expect(document.querySelector('app-kebab-menu')).toBeFalsy();
        });

        it('should use new keybinding when updated', () => {
            fixture.componentInstance.keyBinding = 'F4';
            fixture.detectChanges();
            KeybindingTestUtils.pressKey('F4');

            expect(document.querySelector('app-kebab-menu')).toBeTruthy();
        });
    });
});
