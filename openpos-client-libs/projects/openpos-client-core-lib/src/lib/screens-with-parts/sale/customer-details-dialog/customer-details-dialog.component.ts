import {Component, Injector, OnDestroy, ViewChild} from '@angular/core';
import {CustomerDetailsDialogInterface} from './customer-details-dialog.interface';
import {DialogComponent} from '../../../shared/decorators/dialog-component.decorator';
import {PosScreen} from '../../pos-screen/pos-screen.component';
import {Observable} from 'rxjs';
import {MediaBreakpoints, OpenposMediaService} from '../../../core/media/openpos-media.service';
import {IActionItem} from '../../../core/actions/action-item.interface';
import {Configuration} from '../../../configuration/configuration';
import {KeyPressProvider} from '../../../shared/providers/keypress.provider';
import {ActionService} from '../../../core/actions/action.service';
import { MatTabChangeEvent} from '@angular/material';

@DialogComponent({
  name: 'CustomerDetailsDialog'
})
@Component({
  selector: 'app-customer-details-dialog',
  templateUrl: './customer-details-dialog.component.html',
  styleUrls: ['./customer-details-dialog.component.scss']
})
export class CustomerDetailsDialogComponent extends PosScreen<CustomerDetailsDialogInterface> implements OnDestroy {

  changedToRewardTab = false;
  changedToRewardHistoryTab = false;
  pagedContent: string;
  isMobile: Observable<boolean>;

  constructor(public actionService: ActionService, injector: Injector,
              private media: OpenposMediaService, protected keyPresses: KeyPressProvider) {
    super(injector);
    this.initIsMobile();
  }

  buildScreen() {
  }

  selectContent(content: string): void {
    this.pagedContent = content;
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

  onTabChanged(event: MatTabChangeEvent) {
    if (event.tab.textLabel === this.getRewardsLabel()) {
      this.changedToRewardTab = true;
      this.changedToRewardHistoryTab = false;
    } else if (event.tab.textLabel === this.screen.rewardHistoryLabel) {
      this.changedToRewardHistoryTab = true;
      this.changedToRewardTab = false;
    }
  }

  getRewardsLabel(): string {
    return this.screen.rewardsLabel +
    ((this.screen.customer.numberOfActiveRewards !== undefined) ? ' (' + this.screen.customer.numberOfActiveRewards + ')' : '');
  }

  hasRewards(): boolean {
    return this.screen.customer && this.screen.customer.numberOfActiveRewards && this.screen.customer.numberOfActiveRewards > 0;
  }

  hasRewardsHistory(): boolean {
    return this.screen.customer && this.screen.customer.numberOfHistoricRewards && this.screen.customer.numberOfHistoricRewards > 0;
  }

  public keybindsEnabled(menuItem: IActionItem): boolean {
    return Configuration.enableKeybinds && !!menuItem.keybind && menuItem.keybind !== 'Enter';
  }

}
