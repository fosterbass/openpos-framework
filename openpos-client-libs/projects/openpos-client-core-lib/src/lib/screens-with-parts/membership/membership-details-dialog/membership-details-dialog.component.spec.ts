import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActionService } from '../../../core/actions/action.service';
import { validateExist, validateText } from '../../../utilites/test-utils';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { PhonePipe } from '../../../shared/pipes/phone.pipe';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CLIENTCONTEXT } from '../../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../../core/client-context/time-zone-context';
import { Observable, of, Subscription } from 'rxjs';
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
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { ContentCardComponent } from '../../../shared/components/content-card/content-card.component';
import { DialogHeaderComponent } from '../../../shared/screen-parts/dialog-header/dialog-header.component';
import { PrimaryButtonComponent } from '../../../shared/components/primary-button/primary-button.component';
import { SecondaryButtonComponent } from '../../../shared/components/secondary-button/secondary-button.component';
import { EnrollmentLineItemComponent } from '../../../shared/screen-parts/enrollment-line-item/enrollment-line-item.component';
import { MatCard } from '@angular/material/card';
import { ProgramPlanDetailsComponent } from '../../../shared/screen-parts/program-plan-details/program-plan-details.component';

class MockActionService { }
class MockMatDialog { }
class MockElectronService { }

describe('LinkedCustomerMembershipState', () => {
  let component: MembershipDetailsDialogComponent;
  let fixture: ComponentFixture<MembershipDetailsDialogComponent>;
  let customer;
  let subscriptionAccount: SubscriptionAccount;
  class MockOpenposMediaServiceMobileFalse {
    observe(): Observable<boolean> {
      return of(false);
    }
  }

  beforeEach(() => {
    customer = {
      name: 'Ige Wana',
      email: 'IgeWana@gmail.com',
      phoneNumber: '1118798322',
      loyaltyNumber: 's321111111',
      address: {
        line1: '123 Lizard Lane',
        city: 'Columbus',
        state: 'OH',
        postalCode: '11111'
      }
    };
  });

  describe('shared', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [
          MembershipDetailsDialogComponent,
          PhonePipe,
          MockComponent(IconComponent),
          MockComponent(ContentCardComponent),
          MockComponent(DialogHeaderComponent),
          MockComponent(PrimaryButtonComponent),
          MockComponent(SecondaryButtonComponent),
          MockComponent(EnrollmentLineItemComponent),
          MockComponent(ProgramPlanDetailsComponent),
          MockComponent(MatTabGroup),
          MockComponent(MatTab),
          MockComponent(MatCard)
        ],
        providers: [
          { provide: ActionService, useClass: MockActionService },
          { provide: MatDialog, useClass: MockMatDialog },
          { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
          { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
        ]
      }).compileComponents();
      fixture = TestBed.createComponent(MembershipDetailsDialogComponent);
      component = fixture.componentInstance;
      component.screen = { } as MembershipDetailsDialogInterface;
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
          iconImageUrl: 'iconImageUrl',
          iconText: 'iconText',
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
        fixture.detectChanges();
      });

      describe('tab functionality', () => {
        it('should display tabs', () => {
          validateExist(fixture, '.tabs');
        });

        it('should display subscriptionAccountListTitle', () => {
          validateText(fixture, '.tabs .tab-title', subscriptionAccount.listTitle);
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
          validateExist(fixture, '.tabs .sign-up');
        });
      });
    });
  });
});
