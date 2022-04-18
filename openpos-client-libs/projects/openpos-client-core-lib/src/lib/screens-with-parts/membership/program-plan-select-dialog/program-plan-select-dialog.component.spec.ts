import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActionService } from '../../../core/actions/action.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { MatDialog } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { Observable, of, Subscription } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { ProgramPlanSelectDialogComponent } from './program-plan-select-dialog.component';
import { ProgramPlanSelectDialogInterface } from './program-plan-select-dialog.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { validateExist } from '../../../utilites/test-utils';
import { MockComponent } from 'ng-mocks';
import { DialogHeaderComponent } from '../../../shared/screen-parts/dialog-header/dialog-header.component';
import { ContentCardComponent } from '../../../shared/components/content-card/content-card.component';
import { ProgramPlanDetailsComponent } from '../../../shared/screen-parts/program-plan-details/program-plan-details.component';

class MockActionService { }
class MockMatDialog { }

describe('ProgramPlansSelectDialog', () => {
    let component: ProgramPlanSelectDialogComponent;
    let fixture: ComponentFixture<ProgramPlanSelectDialogComponent>;
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
                    ProgramPlanSelectDialogComponent,
                    MockComponent(DialogHeaderComponent),
                    MockComponent(ContentCardComponent),
                    MockComponent(ProgramPlanDetailsComponent)
                ],
                providers: [
                    { provide: ActionService, useClass: MockActionService },
                    { provide: MatDialog, useClass: MockMatDialog },
                    { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
                    { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
                ]
            }).compileComponents();
            fixture = TestBed.createComponent(ProgramPlanSelectDialogComponent);
            component = fixture.componentInstance;
            component.screen = {
            } as ProgramPlanSelectDialogInterface;
            fixture.detectChanges();
        });

        it('renders', () => {
            expect(component).toBeDefined();
        });

        describe('template', () => {
            it('should have a header', () => {
                validateExist(fixture, 'app-dialog-header');
            });

            it('should display a app-program-plan-details', () => {
                validateExist(fixture, '.program-plan-selection-dialog app-program-plan-details');
            });
        });

        describe('component', () => {
            describe('initIsMobile', () => {
                it('sets the values for isMobile', () => {
                    const media: OpenposMediaService = TestBed.inject(OpenposMediaService);
                    spyOn(media, 'observe');
                    component.initIsMobile();
                    expect(media.observe).toHaveBeenCalledWith(new Map([[MediaBreakpoints.MOBILE_PORTRAIT, true],
                    [MediaBreakpoints.MOBILE_LANDSCAPE, true],
                    [MediaBreakpoints.TABLET_PORTRAIT, true],
                    [MediaBreakpoints.TABLET_LANDSCAPE, true],
                    [MediaBreakpoints.DESKTOP_PORTRAIT, false],
                    [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
                    ]));
                });
            });

            describe('keybindsEnabled ', () => {
                describe('configuration keybinds are enabled', () => {
                    it('and menuItem keybind is not enter', () => {
                        CONFIGURATION.enableKeybinds = true;
                        const menuItem: IActionItem = { keybind: 'NotEnter' } as IActionItem;
                        expect(component.keybindsEnabled(menuItem)).toBeTruthy();
                    });

                    it('and menuItem keybind is Enter', () => {
                        CONFIGURATION.enableKeybinds = true;
                        const menuItem: IActionItem = { keybind: 'Enter' } as IActionItem;
                        expect(component.keybindsEnabled(menuItem)).toBeFalsy();
                    });

                    it('and menuItem keybind is null', () => {
                        CONFIGURATION.enableKeybinds = true;
                        const menuItem: IActionItem = { keybind: null } as IActionItem;
                        expect(component.keybindsEnabled(menuItem)).toBeFalsy();
                    });
                });
                describe('configuration keybinds are disabled', () => {
                    it('and menuItem keybind is not enter', () => {
                        CONFIGURATION.enableKeybinds = false;
                        const menuItem: IActionItem = { keybind: 'NotEnter' } as IActionItem;
                        expect(component.keybindsEnabled(menuItem)).toBeFalsy();
                    });

                    it('and menuItem keybind is Enter', () => {
                        CONFIGURATION.enableKeybinds = false;
                        const menuItem: IActionItem = { keybind: 'Enter' } as IActionItem;
                        expect(component.keybindsEnabled(menuItem)).toBeFalsy();
                    });

                    it('and menuItem keybind is null', () => {
                        CONFIGURATION.enableKeybinds = false;
                        const menuItem: IActionItem = { keybind: null } as IActionItem;
                        expect(component.keybindsEnabled(menuItem)).toBeFalsy();
                    });
                });
            });
        });
    });
});
