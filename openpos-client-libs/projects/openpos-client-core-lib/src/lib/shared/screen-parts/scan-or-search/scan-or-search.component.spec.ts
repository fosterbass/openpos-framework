import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { MockActionService, MockSessionService } from '../../../core/keybindings/keybinding-test.utils';
import { SessionService } from '../../../core/services/session.service';
import { ActionService } from '../../../core/actions/action.service';
import { CONFIGURATION } from '../../../configuration/configuration';
import { MessageProvider } from '../../providers/message.provider';
import { MessageTypes } from '../../../core/messages/message-types';
import { LifeCycleMessage } from '../../../core/messages/life-cycle-message';
import { LifeCycleEvents } from '../../../core/messages/life-cycle-events.enum';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { ScanOrSearchComponent } from './scan-or-search.component';
import { IconSquareButtonComponent } from '../../components/icon-square-button/icon-square-button.component';
import { BarcodeScanner } from '../../../core/platform-plugins/barcode-scanners/barcode-scanner.service';
import { MatDialogModule } from '@angular/material/dialog';

describe('ScanOrSearchComponent', () => {
    let fixture: ComponentFixture<ScanOrSearchComponent>;
    let scanOrSearch: ScanOrSearchComponent;
    let mockActionService: MockActionService;
    let mockSessionService: MockSessionService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                // Not sure why this is needed because it's not used here
                MatDialogModule,
                MatInputModule,
                HttpClientTestingModule,
                NoopAnimationsModule,
                FormsModule
            ],
            declarations: [
                ScanOrSearchComponent,
                IconSquareButtonComponent
            ],
            providers: [
                MessageProvider,
                BarcodeScanner,
                {provide: SessionService, useClass: MockSessionService},
                {provide: ActionService, useClass: MockActionService}
            ]
        }).compileComponents();

        CONFIGURATION.enableKeybinds = true;

        mockSessionService = TestBed.inject(SessionService) as any;
        mockActionService = TestBed.inject(ActionService) as any;

        TestBed.inject(MessageProvider).setMessageType(MessageTypes.SCREEN);
        mockSessionService.dispatchMessage(new LifeCycleMessage(LifeCycleEvents.ScreenUpdated, {
            id: 'take-gregs-money',
            type: MessageTypes.SCREEN
        } as any));

        fixture = TestBed.createComponent(ScanOrSearchComponent);
        scanOrSearch = fixture.componentInstance;

        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should raise the change event when the barcode changes', () => {
        const mockSubscription = jasmine.createSpy('mockSubscription');
        scanOrSearch.change.subscribe(mockSubscription);

        const barcodeInput = fixture.nativeElement.querySelector('.scan-input');
        barcodeInput.value = 'Give me Greg\'s social security number now!';
        barcodeInput.dispatchEvent(new InputEvent('input'));
        fixture.detectChanges();

        expect(mockSubscription).toHaveBeenCalledOnceWith(barcodeInput.value);
    });
});
