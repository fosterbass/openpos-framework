import { TestBed, ComponentFixture } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Subscription } from 'rxjs';
import { ElectronService } from 'ngx-electron';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { ActionService } from '../../../core/actions/action.service';
import { validateIcon, validateText } from '../../../utilites/test-utils';
import { MembershipPointsDisplayComponentInterface } from './membership-points-display.interface';
import { MembershipPointsDisplayComponent } from './membership-points-display.component';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../../components/icon/icon.component';


class MockActionService { }
class MockMatDialog { }
class MockElectronService { }
class ClientContext { }

describe('MembershipPointsDisplayComponent', () => {
    let component: MembershipPointsDisplayComponent;
    let fixture: ComponentFixture<MembershipPointsDisplayComponent>;

    describe('shared', () => {
        beforeEach(() => {
            TestBed.configureTestingModule({
                imports: [HttpClientTestingModule],
                declarations: [
                    MembershipPointsDisplayComponent,
                    MockComponent(IconComponent)
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {} },
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(MembershipPointsDisplayComponent);
            component = fixture.componentInstance;

            component.screenData = {
                membershipPointsIcon: 'some icon',
                pointsLabel: 'some label',
                loyaltyProgramNameLabel: 'loyalty program name',
                customer: {
                    loyaltyPoints: 2
                }
            } as MembershipPointsDisplayComponentInterface;
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('component', () => {
        });

        describe('template', () => {
            it('has an icon that uses screenData.membershipPointsIcon', () => {
                validateIcon(fixture, '.icon app-icon', component.screenData.membershipPointsIcon);
            });
            describe('details', () => {
                it('displays the loyaltyProgramName, loyaltyPoints of the customer, and the unit of the rewards program', () => {
                    const expectedValue = component.screenData.loyaltyProgramNameLabel + ': ' +
                        component.screenData.customer.loyaltyPoints + ' ' +
                        component.screenData.pointsLabel;
                    validateText(fixture, '.details', expectedValue);
                });
            });
        });
    });
});
