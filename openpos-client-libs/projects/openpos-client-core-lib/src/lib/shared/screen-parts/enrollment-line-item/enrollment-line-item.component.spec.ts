import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Observable, of, Subscription } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { ActionService } from '../../../core/actions/action.service';
import { By } from '@angular/platform-browser';
import { EnrollmentLineItemComponent } from './enrollment-line-item.component';
import { ActionItem } from '../../../core/actions/action-item';
import { EnrollmentItem, EnrollmentItemProperty } from '../../../screens-with-parts/membership/enrollment-item-interface';
import { MatDialog } from '@angular/material/dialog';
import { validateExist, validateText } from '../../../utilites/test-utils';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../../components/icon/icon.component';

class MockActionService { }
class MockMatDialog { }

describe('EnrollmentLineItemComponent', () => {
    let component: EnrollmentLineItemComponent;
    let fixture: ComponentFixture<EnrollmentLineItemComponent>;
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
                    EnrollmentLineItemComponent,
                    MockComponent(IconComponent)
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(EnrollmentLineItemComponent);
            component = fixture.componentInstance;
            component.enrollment = {
                title: {
                    icon: 'titleIcon',
                    value: 'value'
                } as EnrollmentItemProperty,
                icon: 'icon',
                details: [
                    { value: 'detailOne' } as EnrollmentItemProperty,
                    { value: 'detailTwo' } as EnrollmentItemProperty
                ],
                actionItemList: [
                    { title: 'actionOne' } as ActionItem,
                    { title: 'actionTwo' } as ActionItem
                ]
            } as EnrollmentItem;
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('component', () => {
            describe('initIsMobile', () => {
                it('sets the values for isMobile', () => {
                    const media: OpenposMediaService = TestBed.inject(OpenposMediaService);
                    spyOn(media, 'observe');

                    component.initIsMobile();

                    expect(media.observe).toHaveBeenCalledWith(new Map([
                        [MediaBreakpoints.MOBILE_PORTRAIT, true],
                        [MediaBreakpoints.MOBILE_LANDSCAPE, true],
                        [MediaBreakpoints.TABLET_PORTRAIT, true],
                        [MediaBreakpoints.TABLET_LANDSCAPE, false],
                        [MediaBreakpoints.DESKTOP_PORTRAIT, false],
                        [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
                    ]));
                });
            });
        });

        describe('template', () => {
            it('should display the title section', () => {
                validateExist(fixture, '.title app-icon');
                validateText(fixture, '.title', component.enrollment.title.value);
            });
            it('should display all details', () => {
                validateText(fixture, '.details', component.enrollment.details[0].value);
                validateText(fixture, '.details', component.enrollment.details[1].value);
            });
            function validateTextQueryAllActionItems(index) {
                const element = fixture.debugElement.queryAll(By.css('.actions button'));
                expect(element[index].nativeElement).toBeDefined();
                expect(element[index].nativeElement.textContent).toContain(component.enrollment.actionItemList[index].title);
            }
            it('should display all actions', () => {
                validateTextQueryAllActionItems(0);
                validateTextQueryAllActionItems(1);
            });
        });
    });
});
