import { MockActionService, MockSessionService } from '../../keybindings/keybinding-test.utils';
import { MatDialogModule } from '@angular/material/dialog';
import { SessionService } from '../../services/session.service';
import { ActionService } from '../../actions/action.service';
import { KeyPressProvider } from '../../../shared/providers/keypress.provider';
import { DisabledKeyPressProvider } from '../../../shared/providers/disabled-keypress.provider';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DialogContentComponent } from './dialog-content.component';
import { KeybindingZoneScreenService } from '../../keybindings/keybinding-zone-screen.service';
import { KeybindingZoneService } from '../../keybindings/keybinding-zone.service';
import { MessageTypes } from '../../messages/message-types';

describe('DialogContentComponent', () => {
    let fixture: ComponentFixture<DialogContentComponent>;
    let dialogContent: DialogContentComponent;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;
    let keybindingZoneService: KeybindingZoneService;
    let keybindingZoneScreenService: KeybindingZoneScreenService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MatDialogModule],
            declarations: [DialogContentComponent],
            providers: [
                KeybindingZoneService,
                KeybindingZoneScreenService,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService},
                {provide: KeyPressProvider, useClass: DisabledKeyPressProvider}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(DialogContentComponent);
        dialogContent = fixture.componentInstance;

        mockActionService = fixture.debugElement.injector.get(ActionService) as any;
        mockSessionService = TestBed.inject(SessionService) as any;
        keybindingZoneService = fixture.debugElement.injector.get(KeybindingZoneService);
        keybindingZoneScreenService = fixture.debugElement.injector.get(KeybindingZoneScreenService);

        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should configure the keybinding zone screen service for DIALOG message', () => {
        expect(keybindingZoneScreenService.getMessageType()).toEqual(MessageTypes.DIALOG);
    });
});
