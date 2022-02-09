import { TestBed, ComponentFixture } from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {Observable, of, Subscription} from 'rxjs';
import {EnrollmentConfirmationDialogComponent} from './enrollment-confirmation-dialog.component';
import {EnrollmentConfirmationDialogInterface} from './enrollment-confirmation-dialog.interface';
import {ActionService} from '../../../core/actions/action.service';
import {MatDialog} from '@angular/material/dialog';
import {OpenposMediaService} from '../../../core/media/openpos-media.service';
import {ElectronService} from 'ngx-electron';
import {TimeZoneContext} from '../../../core/client-context/time-zone-context';
import {CLIENTCONTEXT} from '../../../core/client-context/client-context-provider.interface';
import {IActionItem} from '../../../core/actions/action-item.interface';
import {By} from '@angular/platform-browser';
import { SafeHtmlPipe } from '../../../shared/pipes/safe-html.pipe';
import { MockComponent } from 'ng-mocks';
import { ContentCardComponent } from '../../../shared/components/content-card/content-card.component';
import { DialogHeaderComponent } from '../../../shared/screen-parts/dialog-header/dialog-header.component';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button.component';
import { SecondaryButtonComponent } from '../../../shared/components/secondary-button/secondary-button.component';

class MockActionService {}
class MockMatDialog {}
class MockElectronService {}
class ClientContext {}

describe('EnrollmentConfirmationDialog', () => {
    let component: EnrollmentConfirmationDialogComponent;
    let fixture: ComponentFixture<EnrollmentConfirmationDialogComponent>;
    class MockOpenposMediaServiceMobileFalse {
        observe(): Observable<boolean> {
            return of(false);
        }
    }

    class MockOpenposMediaServiceMobileTrue {
        observe(): Observable<boolean> {
            return of(true);
        }
    }

    beforeEach(() => {
    });

    describe('shared', () => {
        beforeEach( () => {
            TestBed.configureTestingModule({
                imports: [ HttpClientTestingModule],
                declarations: [
                    EnrollmentConfirmationDialogComponent,
                    SafeHtmlPipe,
                    MockComponent(ContentCardComponent),
                    MockComponent(DialogHeaderComponent),
                    MockComponent(PrimaryButtonComponent),
                    MockComponent(SecondaryButtonComponent),
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {}},
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext}
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(EnrollmentConfirmationDialogComponent);
            component = fixture.componentInstance;
            component.screen = {
                programCopy: 'TestProgramCopy',
                continueAction: {} as IActionItem,
                signUpAnother: {} as IActionItem
            } as EnrollmentConfirmationDialogInterface;
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

    });

    describe('non-mobile', () => {
        beforeEach( () => {
            TestBed.configureTestingModule({
                imports: [ HttpClientTestingModule],
                declarations: [
                    EnrollmentConfirmationDialogComponent,
                    SafeHtmlPipe,
                    MockComponent(ContentCardComponent),
                    MockComponent(DialogHeaderComponent),
                    MockComponent(PrimaryButtonComponent),
                    MockComponent(SecondaryButtonComponent),
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {}},
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext}
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(EnrollmentConfirmationDialogComponent);
            component = fixture.componentInstance;
            component.screen = {
                programCopy: 'TestProgramCopy',
                message: 'TestMessage',
                continueAction: {
                    enabled: true,
                    title: 'TestTitle',
                    keybindDisplayName: 'TestKeybindDisplayName'
                } as IActionItem,
                signUpAnother: {
                    enabled: true,
                    title: 'TestTitle'
                } as IActionItem
            } as EnrollmentConfirmationDialogInterface;
            fixture.detectChanges();
        });
        describe('template', () => {
            it('should display the message', () => {
                expect(component.screen.message).toBeDefined();
                expect(component.screen.programCopy).toBeDefined();

                const element = fixture.debugElement.query(By.css('.message'));
                expect(element.nativeElement.textContent).toBeDefined();
            });

            it('should not display the message', () => {
                function helper(message, safeCopy) {
                    component.screen.message = message;
                    component.screen.programCopy = safeCopy;
                    fixture.detectChanges();

                    const element = fixture.debugElement.query(By.css('.message'));
                    expect(element).toBeNull();
                }

                helper('defined', undefined);
                helper(undefined, 'defined');
                helper(undefined, undefined);
            });
            it('should display the primary button', () => {
                const primaryElement = fixture.debugElement.query(By.css('app-primary-button'));
                expect(primaryElement.nativeElement.textContent).toBeDefined();

            });
            it('should not display the primary button', () => {
                component.screen.continueAction = undefined;
                fixture.detectChanges();

                const primaryElement = fixture.debugElement.query(By.css('app-primary-button'));
                expect(primaryElement).toBeNull();

            });
            it('should display the secondary button', () => {
                const secondaryElement = fixture.debugElement.query(By.css('app-secondary-button'));
                expect(secondaryElement.nativeElement.textContent).toBeDefined();
            });

            it('should not display the secondary button', () => {
                component.screen.signUpAnother = undefined;
                fixture.detectChanges();

                const secondaryElement = fixture.debugElement.query(By.css('app-secondary-button'));
                expect(secondaryElement).toBeNull();
            });
        });
    });
});
