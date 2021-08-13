import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core'
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {Observable, of, Subscription} from 'rxjs';
import {EnrollmentConfirmationDialogComponent} from './enrollment-confirmation-dialog.component';
import {EnrollmentConfirmationDialogInterface} from './enrollment-confirmation-dialog.interface';
import {KeyPressProvider} from '../../shared/providers/keypress.provider';
import {ActionService} from '../../core/actions/action.service';
import {MatDialog} from '@angular/material';
import {OpenposMediaService} from '../../core/media/openpos-media.service';
import {ElectronService} from 'ngx-electron';
import {TimeZoneContext} from '../../core/client-context/time-zone-context';
import {CLIENTCONTEXT} from '../../core/client-context/client-context-provider.interface';
import {IActionItem} from '../../core/actions/action-item.interface';
import {By} from "@angular/platform-browser";
import {unwatchFile} from "fs";

class MockKeyPressProvider {
    subscribe(): Subscription {
        return new Subscription();
    }
};

class MockActionService {};
class MockMatDialog {};
class MockElectronService {};
class ClientContext {};

describe('CustomerDetailsDialog', () => {
    let component: EnrollmentConfirmationDialogComponent;
    let fixture: ComponentFixture<EnrollmentConfirmationDialogComponent>;
    let customer;
    class MockOpenposMediaServiceMobileFalse {
        observe(): Observable<boolean> {
            return of(false);
        }
    };

    class MockOpenposMediaServiceMobileTrue {
        observe(): Observable<boolean> {
            return of(true);
        }
    };

    beforeEach(() => {
    });

    describe('shared', () => {
        beforeEach( () => {
            TestBed.configureTestingModule({
                imports: [ HttpClientTestingModule],
                declarations: [
                    EnrollmentConfirmationDialogComponent
                ],
                providers: [
                    { provide: KeyPressProvider, useClass: MockKeyPressProvider },
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {}},
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext}
                ],
                schemas: [
                    NO_ERRORS_SCHEMA,
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(EnrollmentConfirmationDialogComponent);
            component = fixture.componentInstance;
            component.screen = {
                programCopy: 'TestProgramCopy',
                continueAction: {} as IActionItem,
                signUpMorePets: {} as IActionItem
            } as EnrollmentConfirmationDialogInterface;
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('component', () => {
            it('should define the safeProgramCopy on init', function () {
                component.safeProgramCopy = undefined;
                component.ngOnInit();
                expect(component.safeProgramCopy).toBeDefined();
            });
        });
    });

    describe('non-mobile', () => {
        beforeEach( () => {
            TestBed.configureTestingModule({
                imports: [ HttpClientTestingModule],
                declarations: [
                    EnrollmentConfirmationDialogComponent
                ],
                providers: [
                    { provide: KeyPressProvider, useClass: MockKeyPressProvider },
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
                    { provide: ElectronService, useClass: MockElectronService },
                    { provide: ClientContext, useValue: {}},
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext}
                ],
                schemas: [
                    NO_ERRORS_SCHEMA,
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(EnrollmentConfirmationDialogComponent);
            component = fixture.componentInstance;
            component.screen = {
                programCopy: 'TestProgramCopy',
                message: 'TestMessage',
                continueAction: {
                    enabled: true,
                    title: "TestTitle",
                    keybindDisplayName: "TestKeybindDisplayName"
                } as IActionItem,
                signUpMorePets: {
                    enabled: true,
                    title: "TestTitle"
                } as IActionItem
            } as EnrollmentConfirmationDialogInterface;
            fixture.detectChanges();
        });
        describe('template', () => {
            it('should display the message', function () {
                expect(component.screen.message).toBeDefined();
                expect(component.safeProgramCopy).toBeDefined();

                const element = fixture.debugElement.query(By.css('.message'))
                expect(element.nativeElement.textContent).toBeDefined();
            });

            it('should not display the message', function () {
                function helper(message, safeCopy) {
                    component.screen.message = message;
                    component.screen.programCopy = safeCopy;
                    component.ngOnInit();
                    fixture.detectChanges();

                    const element = fixture.debugElement.query(By.css('.message'));
                    expect(element).toBeNull();
                }

                helper("defined", undefined);
                helper(undefined, "defined");
                helper(undefined, undefined);
            });
            it('should display the primary button', function () {
                const primaryElement = fixture.debugElement.query(By.css('app-primary-button'))
                expect(primaryElement.nativeElement.textContent).toBeDefined();

            });
            it('should not display the primary button', function () {
                component.screen.continueAction = undefined;
                fixture.detectChanges();

                const primaryElement = fixture.debugElement.query(By.css('app-primary-button'))
                expect(primaryElement).toBeNull();

            });
            it('should display the secondary button', function () {
                const secondaryElement = fixture.debugElement.query(By.css('app-secondary-button'))
                expect(secondaryElement.nativeElement.textContent).toBeDefined();
            });

            it('should not display the secondary button', function () {
                component.screen.signUpMorePets = undefined;
                fixture.detectChanges();

                const secondaryElement = fixture.debugElement.query(By.css('app-secondary-button'))
                expect(secondaryElement).toBeNull();
            });
        });
    });
});