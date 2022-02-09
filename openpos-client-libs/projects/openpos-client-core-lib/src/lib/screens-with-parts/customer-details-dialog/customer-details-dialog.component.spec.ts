import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CustomerDetailsDialogComponent } from './customer-details-dialog.component';
import { CustomerDetailsDialogInterface } from './customer-details-dialog.interface';
import { ActionService } from '../../core/actions/action.service';
import { validateDoesNotExist, validateExist, validateText } from '../../utilites/test-utils';
import { By } from '@angular/platform-browser';
import { IActionItem } from '../../core/actions/action-item.interface';
import { PhonePipe } from '../../shared/pipes/phone.pipe';
import { MatDialog, MatDialogActions } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ElectronService } from 'ngx-electron';
import { CLIENTCONTEXT } from '../../core/client-context/client-context-provider.interface';
import { TimeZoneContext } from '../../core/client-context/time-zone-context';
import { Observable, of } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../core/media/openpos-media.service';
import { ImageUrlPipe } from '../../shared/pipes/image-url.pipe';
import { MarkdownFormatterPipe } from '../../shared/pipes/markdown-formatter.pipe';
import { MockComponent } from 'ng-mocks';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { InfiniteScrollComponent } from '../../shared/components/infinite-scroll/infinite-scroll.component';
import { DialogHeaderComponent } from '../../shared/screen-parts/dialog-header/dialog-header.component';
import { ContentCardComponent } from '../../shared/components/content-card/content-card.component';
import { CustomerInformationComponent } from '../../shared/screen-parts/customer-information/customer-information.component';
import { DualActionDialogHeaderComponent } from '../../shared/screen-parts/dual-action-dialog-header/dual-action-dialog-header.component';
import { MembershipPointsDisplayComponent } from '../../shared/screen-parts/membership-points-display/membership-points-display.component';
import { SecondaryButtonComponent } from '../../shared/components/secondary-button/secondary-button.component';
import { PrimaryButtonComponent } from '../../shared/components/primary-button/primary-button.component';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { MembershipDisplayComponent } from '../../shared/screen-parts/membership-display/membership-display.component';
import { ActionItemKeyMappingDirective } from '../../shared/directives/action-item-key-mapping.directive';
import { KeybindingZoneService } from '../../core/keybindings/keybinding-zone.service';

class MockActionService { }
class MockMatDialog { }
class MockElectronService { }
class ClientContext { }

describe('CustomerDetailsDialog', () => {
  let component: CustomerDetailsDialogComponent;
  let fixture: ComponentFixture<CustomerDetailsDialogComponent>;
  let customer;
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
    customer = {
      name: 'Bob bobert',
      email: 'b.bobert@gmail.com',
      phoneNumber: '1118798322',
      loyaltyNumber: 's321111111',
      address: {
        line1: '123 Mockingbird Lane',
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
          CustomerDetailsDialogComponent,
          PhonePipe,
          ImageUrlPipe,
          MarkdownFormatterPipe,
          MockComponent(IconComponent),
          MockComponent(InfiniteScrollComponent),
          MockComponent(DialogHeaderComponent),
          MockComponent(ContentCardComponent),
          MockComponent(CustomerInformationComponent),
          MockComponent(DualActionDialogHeaderComponent),
          MockComponent(MembershipPointsDisplayComponent),
          MockComponent(MembershipDisplayComponent),
          MockComponent(SecondaryButtonComponent),
          MockComponent(PrimaryButtonComponent),
          MockComponent(MatTab),
          MockComponent(MatDialogActions),
          MockComponent(MatTabGroup),
          ActionItemKeyMappingDirective
        ],
        providers: [
          KeybindingZoneService,
          { provide: ActionService, useClass: MockActionService },
          { provide: MatDialog, useClass: MockMatDialog },
          { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileFalse },
          { provide: ElectronService, useClass: MockElectronService },
          { provide: ClientContext, useValue: {} },
          { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
        ]
      }).compileComponents();
      fixture = TestBed.createComponent(CustomerDetailsDialogComponent);
      component = fixture.componentInstance;
      component.screen = {
        customer,
        rewardTabEnabled: true,
        rewardHistoryTabEnabled: true
      } as CustomerDetailsDialogInterface;
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
      describe('getRewardsLabel', () => {
        beforeEach(() => {
          component.screen.rewardsLabel = 'Rewards';
        });

        it('returns "Rewards" when the rewards list is undefined', () => {
          component.screen.customer.numberOfActiveRewards = undefined;
          expect(component.getRewardsLabel()).toBe('Rewards');
        });

        it('returns "Rewards (0)" when the rewards list is empty', () => {
          component.screen.customer.numberOfActiveRewards = 0;
          expect(component.getRewardsLabel()).toBe('Rewards (0)');
        });

        it('returns "Rewards (#)" when the rewards list has items', () => {
          component.screen.customer.numberOfActiveRewards = 1;
          expect(component.getRewardsLabel()).toBe('Rewards (1)');
        });
      });
    });

    describe('template', () => {
      describe('user details', () => {
        it('renders the app-customer-information component', () => {
          validateExist(fixture, '.customer-details app-customer-information');
        });
      });
      describe('membership details', () => {
        describe('when membership is disabled', () => {
          beforeEach(() => {
            component.screen.membershipEnabled = false;
            fixture.detectChanges();
          });

          it('does not render the details', () => {
            validateDoesNotExist(fixture, '.memberships');
          });
        });
        describe('when membership is enabled', () => {
          describe('when there are memberships', () => {
            let memberships;
            beforeEach(() => {
              memberships = [
                {}, {}, {}
              ];
              component.screen.customer.memberships = memberships;
              component.screen.membershipEnabled = true;
              component.screen.memberTierLabel = '';
              fixture.detectChanges();
            });

            it('renders the details section', () => {
              const membershipDetailsElement = fixture.debugElement.query(By.css('.memberships'));
              expect(membershipDetailsElement.nativeElement).toBeDefined();
            });

            it('shows the membership label', () => {
              component.screen.membershipLabel = 'some value';
              fixture.detectChanges();
              const membershipLabelElement = fixture.debugElement.query(By.css('.memberships .details-label'));
              expect(membershipLabelElement.nativeElement.textContent).toContain(component.screen.membershipLabel);
            });

            it('shows a membership-display component for each membership', () => {
              const membershipDisplayComponents = fixture.debugElement.queryAll(By.css('app-membership-display'));
              expect(membershipDisplayComponents.length).toBe(memberships.length);
            });
          });

          describe('when there are no memberships', () => {
            beforeEach(() => {
              component.screen.customer.memberships = [];
              component.screen.membershipEnabled = true;
              component.screen.noMembershipsFoundLabel = 'no memberships yet';
              fixture.detectChanges();
            });

            it('shows the noMembershipsFound label', () => {
              validateText(fixture, '.memberships .list', component.screen.noMembershipsFoundLabel);
            });
          });

          describe('membership sign up button', () => {
            let button;
            let configuration;
            const selector = '.membership-sign-up';
            const setButtonConfiguration = (conf) => {
              component.screen.customer.membershipSignUpAction = conf;
            };

            beforeEach(() => {
              configuration = {
                title: 'Some Title'
              } as IActionItem;
              setButtonConfiguration(configuration);
              component.screen.membershipEnabled = true;
              fixture.detectChanges();
              button = fixture.debugElement.query(By.css(selector));
            });

            it('renders when the button configuration is set', () => {
              expect(button.nativeElement).toBeDefined();
            });

            it('does not render when the configuration is undefined', () => {
              setButtonConfiguration(undefined);
              fixture.detectChanges();
              validateDoesNotExist(fixture, selector);
            });

            it('displays the configured text', () => {
              expect(button.nativeElement.innerHTML).toContain(configuration.title);
            });

            it('calls doAction with the configuration when an actionClick event is triggered', () => {
              spyOn(component, 'doAction');
              button = fixture.debugElement.query(By.css(selector));
              button.nativeElement.dispatchEvent(new Event('actionClick'));
              expect(component.doAction).toHaveBeenCalledWith(configuration);
            });

            it('calls doAction with the configuration when the button is clicked', () => {
              spyOn(component, 'doAction');
              button = fixture.debugElement.query(By.css(selector));
              button.nativeElement.click();
              expect(component.doAction).toHaveBeenCalledWith(configuration);
            });
          });

        });
      });
      describe('tabs', () => {
        it('displays the tabs section', () => {
          const tabsElement = fixture.debugElement.query(By.css('.tabs'));
          expect(tabsElement.nativeElement).toBeDefined();
        });
      });
      describe('actions', () => {
        describe('edit button', () => {
          let button;
          let configuration;
          const selector = 'mat-dialog-actions .button';
          const setButtonConfiguration = (conf) => {
            component.screen.doneButton = null;
            component.screen.secondaryButtons = conf ? [conf] : undefined;
          };

          beforeEach(() =>  {
            configuration = {
              title: 'Some Title'
            } as IActionItem;
            setButtonConfiguration(configuration);
            fixture.detectChanges();
            button = fixture.debugElement.query(By.css(selector));
          });

          it('renders when the button configuration is set', () => {
            expect(button.nativeElement).toBeDefined();
          });

          it('does not render when the configuration is undefined', () => {
            setButtonConfiguration(undefined);
            fixture.detectChanges();
            validateDoesNotExist(fixture, selector);
          });

          it('calls doAction with the configuration when an actionClick event is triggered', () => {
            spyOn(component, 'doAction');
            button = fixture.debugElement.query(By.css(selector));
            button.nativeElement.dispatchEvent(new Event('actionClick'));
            expect(component.doAction).toHaveBeenCalledWith(configuration);
          });

          it('calls doAction with the configuration when the button is clicked', () => {
            spyOn(component, 'doAction');
            button = fixture.debugElement.query(By.css(selector));
            button.nativeElement.click();
            expect(component.doAction).toHaveBeenCalledWith(configuration);
          });
        });

        describe('plccLookup button', () => {
          let button;
          let configuration;
          const selector = 'mat-dialog-actions .button';
          const setButtonConfiguration = (conf) => {
             component.screen.doneButton = null;
             component.screen.secondaryButtons = conf ? [conf] : undefined;
           };

          beforeEach(() => {
              configuration = {
                title: 'Some Title'
              } as IActionItem;
              setButtonConfiguration(configuration);
              fixture.detectChanges();
              button = fixture.debugElement.query(By.css(selector));
          });

          it('renders when the button configuration is set', () => {
              expect(button.nativeElement).toBeDefined();
          });

          it('does not render when the configuration is undefined', () => {
              setButtonConfiguration(undefined);
              fixture.detectChanges();
              validateDoesNotExist(fixture, selector);
          });

          it('calls doAction with the configuration when an actionClick event is triggered', () => {
              spyOn(component, 'doAction');
              button = fixture.debugElement.query(By.css(selector));
              button.nativeElement.dispatchEvent(new Event('actionClick'));
              expect(component.doAction).toHaveBeenCalledWith(configuration);
          });

          it('calls doAction with the configuration when the button is clicked', () => {
              spyOn(component, 'doAction');
              button = fixture.debugElement.query(By.css(selector));
              button.nativeElement.click();
              expect(component.doAction).toHaveBeenCalledWith(configuration);
          });
        });

        describe('unlink button', () => {
          let button;
          let configuration;
          const selector = 'mat-dialog-actions .button';
          const setButtonConfiguration = (conf) => {
            component.screen.secondaryButtons = conf ? [conf] : undefined;
          };

          beforeEach(() => {
            configuration = {
              title: 'Some Title'
            } as IActionItem;
            setButtonConfiguration(configuration);
            fixture.detectChanges();
            button = fixture.debugElement.query(By.css(selector));
          });

          it('renders when the button configuration is set', () => {
            expect(button.nativeElement).toBeDefined();
          });

          it('does not render when the configuration is undefined', () => {
            setButtonConfiguration(undefined);
            fixture.detectChanges();
            validateDoesNotExist(fixture, selector);
          });

          it('calls doAction with the configuration when an actionClick event is triggered', () => {
            spyOn(component, 'doAction');
            button = fixture.debugElement.query(By.css(selector));
            button.nativeElement.dispatchEvent(new Event('actionClick'));
            expect(component.doAction).toHaveBeenCalledWith(configuration);
          });

          it('calls doAction with the configuration when the button is clicked', () => {
            spyOn(component, 'doAction');
            button = fixture.debugElement.query(By.css(selector));
            button.nativeElement.click();
            expect(component.doAction).toHaveBeenCalledWith(configuration);
          });
        });
        describe('done button', () => {
          let button;
          let configuration;
          const selector = 'mat-dialog-actions .done';
          const setButtonConfiguration = (conf) => {
            component.screen.secondaryButtons = null;
            component.screen.doneButton = conf;
          };

          beforeEach(() => {
            configuration = {
              title: 'Some Title'
            } as IActionItem;
            setButtonConfiguration(configuration);
            fixture.detectChanges();
            button = fixture.debugElement.query(By.css(selector));
          });

          it('renders when the button configuration is set', () => {
            expect(button.nativeElement).toBeDefined();
          });

          it('does not render when the configuration is undefined', () => {
            setButtonConfiguration(undefined);
            fixture.detectChanges();
            validateDoesNotExist(fixture, selector);
          });

          it('calls doAction with the configuration when an actionClick event is triggered', () => {
            spyOn(component, 'doAction');
            button = fixture.debugElement.query(By.css(selector));
            button.nativeElement.dispatchEvent(new Event('actionClick'));
            expect(component.doAction).toHaveBeenCalledWith(configuration);
          });

          it('calls doAction with the configuration when the button is clicked', () => {
            spyOn(component, 'doAction');
            button = fixture.debugElement.query(By.css(selector));
            button.nativeElement.click();
            expect(component.doAction).toHaveBeenCalledWith(configuration);
          });
        });
      });
    });
  });

  describe('mobile', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [
          CustomerDetailsDialogComponent,
          PhonePipe,
          ImageUrlPipe,
          MarkdownFormatterPipe,
          MockComponent(IconComponent),
          MockComponent(InfiniteScrollComponent),
          MockComponent(DialogHeaderComponent),
          MockComponent(ContentCardComponent),
          MockComponent(CustomerInformationComponent),
          MockComponent(DualActionDialogHeaderComponent),
          MockComponent(MembershipPointsDisplayComponent),
          MockComponent(MembershipDisplayComponent),
          MockComponent(SecondaryButtonComponent),
          MockComponent(PrimaryButtonComponent),
          MockComponent(MatTab),
          MockComponent(MatDialogActions),
          MockComponent(MatTabGroup)
        ],
        providers: [
          { provide: ActionService, useClass: MockActionService },
          { provide: MatDialog, useClass: MockMatDialog },
          { provide: OpenposMediaService, useClass: MockOpenposMediaServiceMobileTrue },
          { provide: ElectronService, useClass: MockElectronService },
          { provide: ClientContext, useValue: {} },
          { provide: CLIENTCONTEXT, useClass: TimeZoneContext }
        ]
      }).compileComponents();
      fixture = TestBed.createComponent(CustomerDetailsDialogComponent);
      component = fixture.componentInstance;
      component.screen = {
        customer,
        rewardTabEnabled: true,
        rewardHistoryTabEnabled: true
      } as CustomerDetailsDialogInterface;
      fixture.detectChanges();
    });
    describe('template', () => {
      it('shows on mobile', () => {
        validateExist(fixture, '.mobile-dialog-container');
      });

      describe('details', () => {
        describe('when membershipPoints is enabled', () => {
          beforeEach(() => {
            component.screen.membershipPointsEnabled = true;
            fixture.detectChanges();
          });

          it('shows the app-membership-points-display', () => {
            validateExist(fixture, '.details-wrapper app-membership-points-display');
          });
        });

        describe('when membershipPoints is disabled', () => {
          beforeEach(() => {
            component.screen.membershipPointsEnabled = false;
            fixture.detectChanges();
          });

          it('does not show the app-membership-points-display', () => {
            validateDoesNotExist(fixture, '.details-wrapper app-membership-points-display');
          });
        });
      });
      describe('paged sections', () => {
        it('show on mobile', () => {
          validateExist(fixture, '.paged-nav-list');
        });

        it('has non-required Rewards and Rewards History sections of selectable content', () => {
          component.screen.rewardTabEnabled = false;
          component.screen.rewardHistoryTabEnabled = false;
          fixture.detectChanges();
          const navListItems = fixture.debugElement.queryAll(By.css('.paged-nav-list-item'));
          expect(navListItems.length).toEqual(1);
          validateExist(fixture, '.paged-nav-list');
        });

        it('has three sections of selectable content', () => {
          component.screen.rewardTabEnabled = true;
          component.screen.rewardHistoryTabEnabled = true;
          fixture.detectChanges();
          const navListItems = fixture.debugElement.queryAll(By.css('.paged-nav-list-item'));
          expect(navListItems.length).toEqual(3);
          validateExist(fixture, '.paged-nav-list');
        });
      });
    });
  });

  describe('non-mobile', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        declarations: [
          CustomerDetailsDialogComponent,
          PhonePipe,
          ImageUrlPipe,
          MarkdownFormatterPipe,
          MockComponent(IconComponent),
          MockComponent(InfiniteScrollComponent),
          MockComponent(DialogHeaderComponent),
          MockComponent(ContentCardComponent),
          MockComponent(CustomerInformationComponent),
          MockComponent(DualActionDialogHeaderComponent),
          MockComponent(MembershipPointsDisplayComponent),
          MockComponent(MembershipDisplayComponent),
          MockComponent(SecondaryButtonComponent),
          MockComponent(PrimaryButtonComponent),
          MockComponent(MatTab),
          MockComponent(MatDialogActions),
          MockComponent(MatTabGroup)
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
      fixture = TestBed.createComponent(CustomerDetailsDialogComponent);
      component = fixture.componentInstance;
      component.screen = {
        customer,
        rewardTabEnabled: true,
        rewardHistoryTabEnabled: true
      } as CustomerDetailsDialogInterface;
      fixture.detectChanges();
    });
    describe('template', () => {
      describe('details', () => {
        it('does not show the app-membership-points-display', () => {
          validateDoesNotExist(fixture, '.details-wrapper app-membership-points-display');
        });
      });
      describe('tabs', () => {
        it('does not has the mobile class', () => {
          const tabsElement = fixture.debugElement.query(By.css('.tabs'));
          expect(tabsElement.nativeElement.classList).not.toContain('mobile');
        });

        it('does not shows the tab section when both rewardTabEnabled and rewardHistoryTab are false', () => {
          component.screen.rewardTabEnabled = false;
          component.screen.rewardHistoryTabEnabled = false;
          fixture.detectChanges();
          validateDoesNotExist(fixture, '.tabs');
        });

        it('it shows when rewardTabEnabled is true', () => {
          component.screen.rewardTabEnabled = true;
          component.screen.rewardHistoryTabEnabled = false;
          fixture.detectChanges();
          validateExist(fixture, '.tabs');
        });

        it('it shows when rewardHistoryTabEnabled is true', () => {
          component.screen.rewardTabEnabled = false;
          component.screen.rewardHistoryTabEnabled = true;
          fixture.detectChanges();
          validateExist(fixture, '.tabs');
        });
      });
    });
  });
});
