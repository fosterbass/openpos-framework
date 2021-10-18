import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { DisplayCustomerLookupComponent } from './display-customer-lookup.component';
import { ICustomerDetails } from '../../../screens-with-parts/customer-search-result-dialog/customer-search-result-dialog.interface';
import { validateText } from '../../../utilites/test-utils';
import { Observable } from 'rxjs';
import { PhonePipe } from '../../pipes/phone.pipe';
import { MatDialog } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ElectronService } from 'ngx-electron';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { By } from '@angular/platform-browser';
import { Membership } from '../membership-display/memebership-display.interface';

class MockMatDialog { }
class MockElectronService { }
class ClientContext { }

@Component({
    selector: 'app-membership-display',
    template: ''
})
export class MockMembershipDisplayComponent {
    @Input()
    membership: Membership;
}

describe('DisplayCustomerLookupComponent', () => {
    let component: DisplayCustomerLookupComponent;
    let fixture: ComponentFixture<DisplayCustomerLookupComponent>;

    const testCustomer = {
        name: 'testName', loyaltyNumber: '12345', phoneNumber: '6142345678',
        email: 'testUser@test.com', address: {
            line1: 'testAddressLine1', line2: 'testAddressLine2',
            city: 'testCity', state: 'testState', postalCode: '12345'
        }, memberships: [
            {
                id: '1', name: 'testGroup', member: false
            } as Membership, {
                id: '2', name: 'testGroup2', member: true
            } as Membership
        ]

    } as ICustomerDetails;

    describe('non mobile', () => {
        beforeEach(async () => {
            await TestBed.configureTestingModule({
                imports: [HttpClientTestingModule],
                declarations: [
                    DisplayCustomerLookupComponent,
                    PhonePipe,
                    MockMembershipDisplayComponent
                ],
                providers: [
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {} },
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
                ],
                schemas: [
                    NO_ERRORS_SCHEMA,
                ]
            }).compileComponents();

            fixture = TestBed.createComponent(DisplayCustomerLookupComponent);
            component = fixture.componentInstance;
            component.customer = testCustomer;
            fixture.detectChanges();

            await fixture.whenStable();
        });
        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('template', () => {
            beforeEach(async () => {
                testCustomer.privacyRestrictedMessage = null;
                fixture.detectChanges();

                await fixture.whenStable();
            });
            it('should display customer name', () => {
                validateText(fixture, '.customer-name', testCustomer.name);
            });
            it('should display customer loyalty number', () => {
                validateText(fixture, '.customer-loyaltyNumber', testCustomer.loyaltyNumber);
            });
            it('should display customer email address', () => {
                validateText(fixture, '.customer-email', testCustomer.email);
            });
            it('should display customer phone number', () => {
                validateText(fixture, '.customer-phoneNumber', '(614) 234-5678');
            });
            it('should display customer address', () => {
                const address = testCustomer.address;

                validateText(fixture, '.customer-line1', address.line1);

                validateText(fixture, '.customer-line2', address.line2);

                const expected = `${address.city}, ${address.state} ${address.postalCode}`;
                validateText(fixture, '.customer-cityStateZip', expected);
            });
            it('should display all memberships as badges', () => {
                const membershipDisplayComponents = fixture.debugElement.queryAll(By.directive(MockMembershipDisplayComponent));
                expect(membershipDisplayComponents.length).toBe(testCustomer.memberships.length);
            });
            it('should display privacy message when privacy is restricted.', () => {
                testCustomer.privacyRestrictedMessage = 'test';
                fixture.detectChanges();
                const privacyDiv = fixture.debugElement.query(By.css('.privacy'));
                expect(privacyDiv).toBeDefined();
                expect(privacyDiv.nativeElement.textContent).toContain(testCustomer.privacyRestrictedMessage);
            });
        });
    });

    describe('mobile', () => {
        beforeEach(async () => {
            await TestBed.configureTestingModule({
                imports: [HttpClientTestingModule],
                declarations: [
                    DisplayCustomerLookupComponent,
                    PhonePipe,
                    MockMembershipDisplayComponent
                ],
                providers: [
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {} },
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
                ],
                schemas: [
                    NO_ERRORS_SCHEMA,
                ]
            }).compileComponents();

            fixture = TestBed.createComponent(DisplayCustomerLookupComponent);
            component = fixture.componentInstance;
            component.customer = testCustomer;
            component.isMobile = new Observable<boolean>(subscriber => {
                subscriber.next(true);
            });
            fixture.detectChanges();
        });
        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('template', () => {
            beforeEach(async () => {
                testCustomer.privacyRestrictedMessage = null;

                fixture.detectChanges();
                await fixture.whenStable();
            });

            it('should display customer name', () => {
                validateText(fixture, '.customer-name', testCustomer.name);
            });
            it('should display customer loyalty number', () => {
                validateText(fixture, '.customer-loyaltyNumber', testCustomer.loyaltyNumber);
            });
            it('should display customer email address', () => {
                validateText(fixture, '.customer-email', testCustomer.email);
            });
            it('should display customer phone number', () => {
                validateText(fixture, '.customer-phoneNumber', '(614) 234-5678');
            });
            it('should display all memberships as badges', () => {
                const membershipDisplayComponents = fixture.debugElement.queryAll(By.directive(MockMembershipDisplayComponent));
                expect(membershipDisplayComponents.length).toBe(testCustomer.memberships.length);
            });
            it('should display privacy message when privacy is restricted.', () => {
                testCustomer.privacyRestrictedMessage = 'test';
                fixture.detectChanges();
                const privacyDiv = fixture.debugElement.query(By.css('.privacy'));
                expect(privacyDiv).toBeDefined();
                expect(privacyDiv.nativeElement.textContent).toContain(testCustomer.privacyRestrictedMessage);
            });
        });
    });
});
