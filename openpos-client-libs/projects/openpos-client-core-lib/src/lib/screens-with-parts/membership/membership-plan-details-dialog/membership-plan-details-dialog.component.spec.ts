import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActionService } from '../../../core/actions/action.service';
import { validateDoesNotExist, validateExist, validateText } from '../../../utilites/test-utils';
import { PhonePipe } from '../../../shared/pipes/phone.pipe';
import { MatDialog } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { Observable, of, Subscription } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { ActionItem } from '../../../core/actions/action-item';
import { MembershipPlanDetailsDialogComponent } from './membership-plan-details-dialog.component';
import { MembershipPlanDetailsDialogInterface } from './membership-plan-details-dialog.interface';
import { MockComponent } from 'ng-mocks';
import { DialogHeaderComponent } from '../../../shared/screen-parts/dialog-header/dialog-header.component';
import { ContentCardComponent } from '../../../shared/components/content-card/content-card.component';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button.component';
import { EnrollmentLineItemComponent } from '../../../shared/screen-parts/enrollment-line-item/enrollment-line-item.component';
import { PlanDetailsDisplayComponent } from '../../../shared/screen-parts/plan-details-display/plan-details-display.component';

class MockActionService { }
class MockMatDialog { }

describe('LinkedCustomerMembershipState - Plan Details Dialog', () => {
    let component: MembershipPlanDetailsDialogComponent;
    let fixture: ComponentFixture<MembershipPlanDetailsDialogComponent>;

    class MockOpenposMediaServiceMobileFalse {
        observe(): Observable<boolean> {
            return of(false);
        }
    }

    describe('shared', () => {
        beforeEach(() => {
            TestBed.configureTestingModule({
                imports: [HttpClientTestingModule],
                declarations: [
                    MembershipPlanDetailsDialogComponent,
                    PhonePipe,
                    MockComponent(DialogHeaderComponent),
                    MockComponent(ContentCardComponent),
                    MockComponent(PrimaryButtonComponent),
                    MockComponent(EnrollmentLineItemComponent),
                    MockComponent(PlanDetailsDisplayComponent)
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(MembershipPlanDetailsDialogComponent);
            component = fixture.componentInstance;
            component.screen = {
                doneButton: { title: 'Done', enabled: true } as ActionItem
            } as MembershipPlanDetailsDialogInterface;
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('component', () => {
            it('sets the values for isMobile', () => {
                const media: OpenposMediaService = TestBed.inject(OpenposMediaService);
                spyOn(media, 'observe');

                component.initIsMobile();

                expect(media.observe).toHaveBeenCalledWith(new Map([
                    [MediaBreakpoints.MOBILE_PORTRAIT, true],
                    [MediaBreakpoints.MOBILE_LANDSCAPE, true],
                    [MediaBreakpoints.TABLET_PORTRAIT, true],
                    [MediaBreakpoints.TABLET_LANDSCAPE, true],
                    [MediaBreakpoints.DESKTOP_PORTRAIT, false],
                    [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
                ]));
            });

            describe('template', () => {
                beforeEach(() => {
                    fixture.detectChanges();
                });

                it('renders the enrollment line item component', () => {
                    validateExist(fixture, '.plan-details-dialog-enrollment');
                });

                it('renders the plan details component', () => {
                    validateExist(fixture, '.plan-details-dialog-plan');
                });

                it('has an optional done button', () => {
                    component.screen.doneButton = undefined;
                    fixture.detectChanges();
                    validateDoesNotExist(fixture, '.plan-details-dialog-done-button');
                });

                it('renders the done button', () => {
                    validateText(fixture, '.plan-details-dialog-done-button', component.screen.doneButton.title);
                });
            });
        });
    });
});
