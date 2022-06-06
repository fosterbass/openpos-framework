import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActionService } from '../../../core/actions/action.service';
import { validateExist, validateText } from '../../../utilites/test-utils';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { PhonePipe } from '../../../shared/pipes/phone.pipe';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ElectronService } from 'ngx-electron';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { Observable, of } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { MembershipDetailsDialogComponent } from './membership-details-dialog.component';
import { ActionItem } from '../../../core/actions/action-item';
import { CONFIGURATION } from '../../../configuration/configuration';
import { MembershipDetailsDialogInterface } from './membership-details-dialog.interface';
import { MatDialog } from '@angular/material/dialog';
import { SubscriptionAccount } from '../subscription-account-interface';
import { EnrollmentItem } from '../enrollment-item-interface';
import { Plan } from '../plan-interface';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { ContentCardComponent } from '../../../shared/components/content-card/content-card.component';
import { DialogHeaderComponent } from '../../../shared/screen-parts/dialog-header/dialog-header.component';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button.component';
import { SecondaryButtonComponent } from '../../../shared/components/secondary-button/secondary-button.component';
import { EnrollmentLineItemComponent } from '../../../shared/screen-parts/enrollment-line-item/enrollment-line-item.component';
import { ProgramPlanDetailsComponent } from '../../../shared/screen-parts/program-plan-details/program-plan-details.component';
import { TabbedContentCardComponent } from '../../../shared/components/tabbed-content-card/tabbed-content-card.component';
import { ITab } from '../../../shared/components/tabbed-content-card/tab.interface';

class MockActionService { }
class MockMatDialog { }
class MockElectronService { }
class ClientContext { }

describe('LinkedCustomerMembershipState', () => {
  let component: MembershipDetailsDialogComponent;
  let fixture: ComponentFixture<MembershipDetailsDialogComponent>;
  let subscriptionAccount: SubscriptionAccount;
  let tabs: ITab[];
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
          MembershipDetailsDialogComponent,
          TabbedContentCardComponent,
          PhonePipe,
          MockComponent(IconComponent),
          MockComponent(ContentCardComponent),
          MockComponent(DialogHeaderComponent),
          MockComponent(PrimaryButtonComponent),
          MockComponent(SecondaryButtonComponent),
          MockComponent(EnrollmentLineItemComponent),
          MockComponent(ProgramPlanDetailsComponent),
        ],
        providers: [
          { provide: ActionService, useClass: MockActionService },
          { provide: MatDialog, useClass: MockMatDialog },
          { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
          { provide: ElectronService, useClass: MockElectronService },
          { provide: ClientContext, useValue: {} },
          { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
        ]
      }).compileComponents();
      fixture = TestBed.createComponent(MembershipDetailsDialogComponent);
      component = fixture.componentInstance;
      component.screen = {} as MembershipDetailsDialogInterface;
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

    describe('template', () => {
      beforeEach(() => {
        const subscriptionAccounts: SubscriptionAccount[] = [];
        const enrollmentItem: EnrollmentItem = {} as EnrollmentItem;
        const plan: Plan = {} as Plan;
        subscriptionAccount = {
          customerProgramId: 'programId',
          enrollmentItems: [enrollmentItem],
          listTitle: 'listTitle',
          plans: [plan],
          signupActionItem: {
            enabled: true,
            icon: 'signupActionItem.icon',
            title: 'signupActionItem.title'
          } as ActionItem
        } as SubscriptionAccount;
        subscriptionAccounts.push(subscriptionAccount);
        component.screen.subscriptionAccounts = subscriptionAccounts;
        tabs = [
          {tabId: 'programId', label: 'label', icon: 'icon'}
        ];
        component.screen.tabs = tabs;
        fixture.detectChanges();
      });

      describe('tab functionality', () => {
        it('should display tabs', () => {
          validateExist(fixture, '.tabs');
          const tabsElement = fixture.nativeElement.querySelector('#tabbedContent');
          expect(tabsElement).toBeDefined();
          expect(tabsElement).not.toBeNull();
        });

        it('should display subscriptionAccountListTitle', () => {
          validateText(fixture, '.tab-title', subscriptionAccount.listTitle);
        });

        it('should display app-enrollment-line-items', () => {
          validateExist(fixture, '.tabs app-enrollment-line-item');
        });

        it('should display app-program-plan-details', () => {
          component.screen.subscriptionAccounts[0].enrollmentItems = null;
          fixture.detectChanges();

          validateExist(fixture, 'app-program-plan-details');
        });

        it('should display have sign-up', () => {
          validateExist(fixture, '.sign-up');
        });
      });
    });
  });
});
