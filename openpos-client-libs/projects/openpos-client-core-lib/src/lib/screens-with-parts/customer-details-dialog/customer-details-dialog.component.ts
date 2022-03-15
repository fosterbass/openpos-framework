import { Component, Injector } from '@angular/core';
import { FormControl } from '@angular/forms';
import { CustomerDetailsDialogInterface, CustomerItemHistoryFilter } from './customer-details-dialog.interface';
import { Observable } from 'rxjs';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { DialogComponent } from '../../shared/decorators/dialog-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { KeyPressProvider } from '../../shared/providers/keypress.provider';
import { MediaBreakpoints, OpenposMediaService } from '../../core/media/openpos-media.service';
import { IActionItem } from '../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../configuration/configuration';


@DialogComponent({
  name: 'CustomerDetailsDialog'
})
@Component({
  selector: 'app-customer-details-dialog',
  templateUrl: './customer-details-dialog.component.html',
  styleUrls: ['./customer-details-dialog.component.scss']
})
export class CustomerDetailsDialogComponent extends PosScreenDirective<CustomerDetailsDialogInterface> {

  changedToRewardTab = true;
  changedToRewardHistoryTab = false;
  changedToItemHistoryTab = false;
  pagedContent: string;
  isMobile: Observable<boolean>;
  readonly itemsHistoryFilterController = new ItemsHistoryFilterController(this);

  constructor(
    injector: Injector,
    protected keyPresses: KeyPressProvider,
    private media: OpenposMediaService
  ) {
    super(injector);
    this.initIsMobile();
  }

  buildScreen() {
    if (this.screen.itemHistoryEnabled) {
      this.itemsHistoryFilterController.build();
    }
  }

  selectContent(content: string): void {
    this.pagedContent = content;
  }

  initIsMobile(): void {
    this.isMobile = this.media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, false],
      [MediaBreakpoints.TABLET_LANDSCAPE, false],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }

  onTabChanged(event: MatTabChangeEvent) {
    if (event.tab.textLabel === this.getRewardsLabel()) {
      this.changedToRewardTab = true;
      this.changedToRewardHistoryTab = false;
      this.changedToItemHistoryTab = false;
    } else if (event.tab.textLabel === this.screen.rewardHistoryLabel) {
      this.changedToRewardHistoryTab = true;
      this.changedToRewardTab = false;
      this.changedToItemHistoryTab = false;
    } else if (event.tab.textLabel === this.screen.itemHistoryLabel) {
      this.changedToItemHistoryTab = true;
      this.changedToRewardTab = false;
      this.changedToRewardHistoryTab = false;
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
    return CONFIGURATION.enableKeybinds && !!menuItem.keybind && menuItem.keybind !== 'Enter';
  }

  hasAnyDisplayableCustomerInformation(): boolean {
      const customer = this.screen.customer;
      return customer != null && (
          !!customer.email ||
          !!customer.phoneNumber ||
          !!customer.loyaltyNumber ||
          !!customer.address ||
          !!customer.birthDate
      );
    }
}

class ItemsHistoryFilterController {
  fromDate = new FormControl();
  toDate = new FormControl();
  textFilter = new FormControl();

  private get serverState() {
    return this._parent.screen.itemHistoryFilter;
  }

  constructor(private readonly _parent: CustomerDetailsDialogComponent) { }

  fromDateFilterChanged(value: Date) {
    this.doItemHistoryFilterAction({ ...this.serverState, fromDate: value.toISOString() });
  }

  toDateFilterChanged(value: Date) {
    this.doItemHistoryFilterAction({ ...this.serverState, toDate: value.toISOString() });
  }

  onFilterKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      if (event.target instanceof HTMLInputElement) {
        event.target.blur();
        this.filterItemHistory(event.target.value);
      }
    }
  }

  filterItemHistory(value?: string): void {
    value = value || this.textFilter.value;
    this.doItemHistoryFilterAction({ ...this.serverState, text: value });
  }

  build() {
    this.fromDate.setValue(this.serverState.fromDate);
    this.toDate.setValue(this.serverState.toDate);
    this.textFilter.setValue(this.serverState.text);
  }

  private doItemHistoryFilterAction(filter: CustomerItemHistoryFilter) {
    this._parent.doAction('ItemHistoryFilterChanged', filter);
  }
}
