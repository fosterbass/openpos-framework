import { AfterContentChecked, ChangeDetectorRef, Component, Injector } from '@angular/core';
import { MembershipDetailsDialogInterface } from './membership-details-dialog.interface';
import { DialogComponent } from '../../../shared/decorators/dialog-component.decorator';
import { PosScreenDirective } from '../../../screens-with-parts/pos-screen/pos-screen.component';
import { Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { ActionService } from '../../../core/actions/action.service';
import { SubscriptionAccount } from '../subscription-account-interface';

@DialogComponent({
  name: 'MembershipDetailsDialog'
})
@Component({
  selector: 'app-membership-details-dialog',
  templateUrl: './membership-details-dialog.component.html',
  styleUrls: ['./membership-details-dialog.component.scss']
})
export class MembershipDetailsDialogComponent extends PosScreenDirective<MembershipDetailsDialogInterface> implements AfterContentChecked {

  isMobile: Observable<boolean>;

  selectedTab: SubscriptionAccount;

  constructor(public actionService: ActionService, injector: Injector,
              private media: OpenposMediaService,
              private changeDetection: ChangeDetectorRef) {
    super(injector);
    this.initIsMobile();
  }

  ngAfterContentChecked() {
    this.changeDetection.detectChanges();
  }

  initIsMobile(): void {
    this.isMobile = this.media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, true],
      [MediaBreakpoints.TABLET_LANDSCAPE, true],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }

  buildScreen() {

  }

  public updateSelectedTab(val: string): void {
    if (val) {
      const tabs = this.screen.subscriptionAccounts.filter(t => t.customerProgramId === val);
      if (tabs && tabs.length > 0) {
        this.selectedTab = tabs[0];
      }
    } else {
      this.selectedTab = undefined;
    }
  }
  public keybindsEnabled(menuItem: IActionItem): boolean {
    return CONFIGURATION.enableKeybinds && !!menuItem.keybind && menuItem.keybind !== 'Enter';
  }
}
