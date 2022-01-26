import { ComponentFixture, TestBed } from '@angular/core/testing';
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
import { MessageProvider } from '../../providers/message.provider';
import { MessageTypes } from '../../../core/messages/message-types';
import { LifeCycleMessage } from '../../../core/messages/life-cycle-message';
import { LifeCycleEvents } from '../../../core/messages/life-cycle-events.enum';
import { MatDialogModule } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatInputModule } from '@angular/material/input';
import { PromptFormPartComponent } from './prompt-form-part.component';
import { InstructionsComponent } from '../../components/instructions/instructions.component';
import { PromptInputComponent } from '../../components/prompt-input/prompt-input.component';
import { ContentCardComponent } from '../../components/content-card/content-card.component';
import { PrimaryButtonComponent } from '../../components/primary-button/primary-button.component';
import { SecondaryButtonComponent } from '../../components/secondary-button/secondary-button.component';
import { DisplayPropertyComponent } from '../../components/display-property/display-property.component';
import { IconComponent } from '../../components/icon/icon.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BarcodeScanner } from '../../../core/platform-plugins/barcode-scanners/barcode-scanner.service';

describe('PromptFormPartComponent', () => {
    let fixture: ComponentFixture<PromptFormPartComponent>;
    let promptFormPart: PromptFormPartComponent;
    let keybindingZoneService: KeybindingZoneService;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                MatDialogModule,
                MatFormFieldModule,
                MatInputModule,
                HttpClientTestingModule,
                NoopAnimationsModule,
                FormsModule,
                ReactiveFormsModule
            ],
            declarations: [
                PromptFormPartComponent,
                InstructionsComponent,
                PromptInputComponent,
                ContentCardComponent,
                PrimaryButtonComponent,
                SecondaryButtonComponent,
                DisplayPropertyComponent,
                IconComponent
            ],
            providers: [
                KeybindingService,
                KeybindingZoneService,
                KeybindingParserService,
                MessageProvider,
                BarcodeScanner,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        mockSessionService = TestBed.inject(SessionService) as any;
        mockActionService = TestBed.inject(ActionService) as any;

        keybindingZoneService = TestBed.inject(KeybindingZoneService);
        keybindingZoneService.register({
            id: 'take-gregs-money',
            actionsObj: {
                takeGregsMoney: {
                    keybind: 'Enter',
                    action: 'TakeIt!!!!!'
                }
            }
        });
        keybindingZoneService.activate();

        TestBed.inject(MessageProvider).setMessageType(MessageTypes.SCREEN);
        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
            id: 'take-gregs-money',
            type: MessageTypes.SCREEN
        } as any));

        fixture = TestBed.createComponent(PromptFormPartComponent);
        promptFormPart = fixture.componentInstance;

        fixture.detectChanges();
        await fixture.whenStable();
    });

    describe('keybindings', () => {
        it('should remove an existing Enter keybinding', () => {
            KeybindingTestUtils.pressKey('Enter');
            expect(mockActionService.doAction).not.toHaveBeenCalled();
        });
    });
});
