import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Observable, of } from 'rxjs';
import { SaleTotalPanelComponent } from './sale-total-panel.component';
import { MatDialog } from '@angular/material/dialog';
import { ElectronService } from 'ngx-electron';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { ActionService } from '../../../core/actions/action.service';
import { OpenposMediaService } from '../../../core/media/openpos-media.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { validateDoesNotExist } from '../../../utilites/test-utils';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../../components/icon/icon.component';
import { SaleTotalPanelInterface } from './sale-total-panel.interface';


class MockMatDialog { }
class MockActionService { }
class MockElectronService { }
class ClientContext { }

describe('SaleTotalPanelComponent', () => {
    let component: SaleTotalPanelComponent;
    let fixture: ComponentFixture<SaleTotalPanelComponent>;
    let openposMediaSerivce: OpenposMediaService;

    describe('non mobile', () => {
        class MockOpenposMediaServiceMobile {
            observe(): Observable<boolean> {
                return of(false);
            }
        }

        beforeEach(async () => {
            await TestBed.configureTestingModule({
                imports: [HttpClientTestingModule],
                declarations: [
                    SaleTotalPanelComponent,
                    ImageUrlPipe,
                    MockComponent(IconComponent)
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobile },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {} },
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(SaleTotalPanelComponent);
            component = fixture.componentInstance;
            component.screenData = {
                statusMessage: 'active'
            } as SaleTotalPanelInterface;
            openposMediaSerivce = TestBed.inject(OpenposMediaService);
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('component', () => {
            describe('keybindsEnabled', () => {
                let menuItem;
                beforeEach(() => {
                    CONFIGURATION.enableKeybinds = true;
                    menuItem = {
                        keybind: 'F5'
                    };
                });
                it('returns false when CONFIGURATION.enableKeybinds is false', () => {
                    CONFIGURATION.enableKeybinds = false;
                    expect(component.keybindsEnabled(menuItem)).toBe(false);
                });

                it('returns false when CONFIGURATION.enableKeybinds is true and the menuItem.keybind is falsey', () => {
                    menuItem.keybind = undefined;
                    expect(component.keybindsEnabled(menuItem)).toBe(false);
                });

                it(`returns false when CONFIGURATION.enableKeybinds is true and the menuItem.keybind is truthy and is 'Enter'`, () => {
                    menuItem.keybind = 'Enter';
                    expect(component.keybindsEnabled(menuItem)).toBe(false);
                });

                it(`returns true when the CONFIGURATION.enableKeybinds is true and the menuItem.keybind is truthy and not 'Enter'`, () => {
                    expect(component.keybindsEnabled(menuItem)).toBe(true);
                });
            });
        });

        describe('template', () => {
            describe('sale-total-header', () => {
                const configureComponent =
                    (readOnly: boolean, loyaltyButton: IActionItem, customer = undefined) => {
                        component.screenData.readOnly = readOnly;
                        component.screenData.loyaltyButton = loyaltyButton;
                        component.screenData.customer = customer;
                        fixture.detectChanges();
                    };

                describe('when read only', () => {
                    beforeEach(() => {
                        configureComponent(true, {} as IActionItem);
                    });

                    it('does not show the link customer button', () => {
                        validateDoesNotExist(fixture, 'app-sale-loyalty-part');
                    });
                });
                describe('when there is no screen data for the loyaltyButton', () => {
                    beforeEach(() => {
                        configureComponent(false, undefined);
                    });
                    it('does not show the link customer button', () => {
                        validateDoesNotExist(fixture, 'app-sale-loyalty-part');
                    });
                });
            });
        });
    });
});
