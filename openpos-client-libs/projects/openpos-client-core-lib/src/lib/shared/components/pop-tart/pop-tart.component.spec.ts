import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CONFIGURATION } from '../../../configuration/configuration';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { KeybindingService } from '../../../core/keybindings/keybinding.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PopTartComponent } from './pop-tart.component';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { OptionButtonComponent } from '../option-button/option-button.component';
import { MatIconModule } from '@angular/material/icon';
import { IconComponent } from '../icon/icon.component';
import { IconService } from '../../../core/services/icon.service';
import { OpenposMediaService } from '../../../core/media/openpos-media.service';
import { EMPTY } from 'rxjs';
import { ArrowTabDirective } from '../../directives/arrow-tab.directive';
import { ArrowTabItemDirective } from '../../directives/arrow-tab-item.directive';
import { AfterViewInit, Component,
    ComponentFactory,
    ComponentFactoryResolver,
    TemplateRef,
    Type,
    ViewChild,
    ViewContainerRef
} from '@angular/core';

function createDialogData(): any {
    return {
        searchable: false,
        instructions: 'Take Greg\'s Money!',
        optionItems: [
            'Take it All',
            'Take Some of it',
            'Take Most of it'
        ]
    };
}

function clickOptionAt(fixture: ComponentFixture<any>, index: number): void {
    const buttons = fixture.nativeElement.querySelectorAll('app-option-button');
    buttons[index].dispatchEvent(new Event('click'));
}

describe('PopTartComponent', () => {
    let fixture: ComponentFixture<PopTartComponent>;
    let keybindingService: KeybindingService;
    let popTart: PopTartComponent;
    const data = createDialogData();
    let dialogRef: any;
    const mockIconService = jasmine.createSpyObj('MockIconService', ['getIconHtml']);
    const mockOpenposMediaService = jasmine.createSpyObj('MockOpenposMediaService', ['observe']);

    mockIconService.getIconHtml.and.returnValue(EMPTY);
    mockOpenposMediaService.observe.and.returnValue(EMPTY);

    beforeEach(async () => {
        dialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

        await TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                MatIconModule,
                MatFormFieldModule,
                HttpClientTestingModule,
                NoopAnimationsModule
            ],
            declarations: [
                PopTartComponent,
                OptionButtonComponent,
                IconComponent,
                ArrowTabDirective,
                ArrowTabItemDirective
            ],
            providers: [
                KeybindingService,
                KeybindingZoneService,
                MatDialog,
                {provide: MatDialogRef, useValue: dialogRef},
                {provide: MAT_DIALOG_DATA, useValue: data},
                {provide: IconService, useValue: mockIconService},
                {provide: OpenposMediaService, useValue: mockOpenposMediaService},
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;
        // Register just for testing restoring the previously active zone
        keybindingService = TestBed.inject(KeybindingService);
        keybindingService.register('previously-active');
        keybindingService.activate('previously-active');

        fixture = TestBed.createComponent(PopTartComponent);
        popTart = fixture.componentInstance;

        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should select an item when clicking it', () => {
        clickOptionAt(fixture, 1);
        expect(dialogRef.close).toHaveBeenCalledOnceWith(data.optionItems[1]);
    });

    it('should close the dialog after selecting an item', () => {
        clickOptionAt(fixture, 0);
        expect(dialogRef.close).toHaveBeenCalledOnceWith(data.optionItems[0]);
    });

    describe('keybindings', () => {
        it('should register and activate keybinding zone', () => {
            expect(popTart.keybindingZoneService.isRegistered()).toBeTrue();
            expect(popTart.keybindingZoneService.isActive()).toBeTrue();
        });

        it('should restore the previously active zone after selecting an item', () => {
            clickOptionAt(fixture, 0);
            expect(keybindingService.getActiveZoneId()).toEqual('previously-active');
        });
    });
});
