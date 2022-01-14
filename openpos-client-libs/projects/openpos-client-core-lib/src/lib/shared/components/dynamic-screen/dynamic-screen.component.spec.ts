import { TestBed, ComponentFixture } from '@angular/core/testing';
import { DynamicScreenComponent } from './dynamic-screen.component';
import { MessageProvider } from '../../providers/message.provider';
import { ToastrService } from 'ngx-toastr';
import { SessionService } from '../../../core/services/session.service';
import { ConfigurationService } from '../../../core/services/configuration.service';
import { BehaviorSubject, Observable } from 'rxjs';
import { MockComponent } from 'ng-mocks';
import { LoaderComponent } from '../loader/loader.component';
import { WatermarkMessage } from '../../../core/messages/watermark-message';
import { MessageTypes } from '../../../core/messages/message-types';
import { UIConfigMessage } from '../../../core/messages/ui-config-message';
import { UIMessage } from '../../../core/messages/ui-message';
import { StatusBarComponent } from '../../status/status-bar/status-bar.component';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';
import { KeybindingZoneScreenService } from '../../../core/keybindings/keybinding-zone-screen.service';

describe('DynamicScreenComponent', () => {
    let fixture: ComponentFixture<DynamicScreenComponent>;
    let component: DynamicScreenComponent;
    const hideWatermarkMessage = new BehaviorSubject<WatermarkMessage>(undefined);
    const showWatermarkMessage = new BehaviorSubject<WatermarkMessage>(undefined);
    const uiConfig = new BehaviorSubject<UIConfigMessage>(undefined);
    const uiMessage = new BehaviorSubject<UIMessage>(undefined);
    const localTheme = new BehaviorSubject<string>('');
    let messageType: string;

    const mockService = {
        start: (): void => {},
        setMessageType: (type: string): void => {
            messageType = type;
        },
        getMessages: (messagesType: string): Observable<WatermarkMessage> => {
            if (messagesType === MessageTypes.HIDE_WATERMARK) {
                return hideWatermarkMessage.asObservable();
            } else if (messagesType === MessageTypes.WATERMARK) {
                return showWatermarkMessage.asObservable();
            }
        },
        getConfiguration: () => uiConfig.asObservable(),
        getScopedMessages$: () => uiMessage.asObservable(),
        theme$: localTheme
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [DynamicScreenComponent, MockComponent(LoaderComponent), MockComponent(StatusBarComponent)],
            providers: [KeybindingZoneService, KeybindingZoneScreenService]
        });
        TestBed.overrideComponent(DynamicScreenComponent, {
            set: {
                providers: [
                    { provide: ConfigurationService, useValue: mockService },
                    { provide: MessageProvider, useValue: mockService },
                    { provide: ToastrService, useValue: mockService },
                    { provide: SessionService, useValue: mockService },
                    { provide: KeybindingZoneService, useValue: mockService },
                    { provide: KeybindingZoneScreenService, useValue: mockService }
                ]
            }
        }).compileComponents();

        fixture = TestBed.createComponent(DynamicScreenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('creates', () => {
        expect(component).toBeTruthy();
    });
    describe('constructor', () => {
        it('sets message type to Screen', () => {
            expect(messageType).toBe('Screen');
        });
        it('shows the watermark', () => {
            component.showWatermark = false;
            showWatermarkMessage.next({ type: MessageTypes.WATERMARK, screenMessage: 'watermark' });
            expect(component.showWatermark).toBeTruthy();
            expect(component.watermarkMessage).toBe('watermark');
        });
        it('hides the watermark', () => {
            component.showWatermark = true;
            component.watermarkMessage = 'watermark';
            hideWatermarkMessage.next({ type: MessageTypes.HIDE_WATERMARK, screenMessage: 'watermark' });
            expect(component.showWatermark).toBeFalsy();
            expect(component.watermarkMessage).toBe('');
        });
        it('shows the status bar based on appId', () => {
            component.showStatusBarAppId = false;
            uiConfig.next({ showStatusBar: 'true' } as UIConfigMessage);
            expect(component.showStatusBarAppId).toBeTruthy();
        });
        it('hides the status bar based on appId', () => {
            component.showStatusBarAppId = true;
            uiConfig.next({ showStatusBar: 'false' } as UIConfigMessage);
            expect(component.showStatusBarAppId).toBeFalsy();
        });
        it('shows the status bar based on state', () => {
            component.showStatusBarState = false;
            uiMessage.next({ showStatusBar: true } as UIMessage);
            expect(component.showStatusBarState).toBeTruthy();
        });
        it('hides the status bar based on state', () => {
            component.showStatusBarState = true;
            uiMessage.next({ showStatusBar: false } as UIMessage);
            expect(component.showStatusBarState).toBeFalsy();
        });
    });

    describe('getLocalTheme()', () => {
        it('returns the theme from ConfigurationService', () => {
            expect(component.getLocalTheme()).toBe(localTheme);
        });
    });
});
