import { AfterContentChecked, ChangeDetectorRef, Component, Injector, TemplateRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { CustomerDetailsDialogInterface, CustomerItemHistoryFilter } from './customer-details-dialog.interface';
import { Observable } from 'rxjs';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { DialogComponent } from '../../shared/decorators/dialog-component.decorator';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { MediaBreakpoints, OpenposMediaService } from '../../core/media/openpos-media.service';
import { IActionItem } from '../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../configuration/configuration';
import { ITab } from '../../shared/components/tabbed-content-card/tab.interface';


@DialogComponent({
  name: 'CustomerDetailsDialog'
})
@Component({
  selector: 'app-customer-details-dialog',
  templateUrl: './customer-details-dialog.component.html',
  styleUrls: ['./customer-details-dialog.component.scss']
})
export class CustomerDetailsDialogComponent extends PosScreenDirective<CustomerDetailsDialogInterface> implements AfterContentChecked{

  changedToRewardTab = true;
  changedToRewardHistoryTab = false;
  changedToItemHistoryTab = false;
  isMobile: Observable<boolean>;
  readonly itemsHistoryFilterController = new ItemsHistoryFilterController(this);

  @ViewChild('rewards') rewardsTemplate: TemplateRef<any>;
  @ViewChild('rewardsHistory') rewardsHistoryTemplate: TemplateRef<any>;
  @ViewChild('itemHistory') itemHistoryTemplate: TemplateRef<any>;

  public selectedTabValue: string;
  public selectedTab: ITab;

  constructor(
    injector: Injector,
    private media: OpenposMediaService,
    private changeDetection: ChangeDetectorRef
  ) {
    super(injector);
    this.initIsMobile();
  }

  ngAfterContentChecked() {
    this.changeDetection.detectChanges();
  }

  buildScreen() {
    if (this.screen.itemHistoryFilter) {
      this.itemsHistoryFilterController.build();
    }
  }

  public updateSelectedTab(val: string): void {
    this.selectedTabValue = val;
    if (val) {
      const tabs = this.screen.tabs.filter(t => t.tabId === val);
      if (tabs?.length > 0) {
        this.selectedTab = tabs[0];
      }
    } else {
      this.selectedTab = undefined;
    }
  }

  public getSelectedTabTemplate(): TemplateRef<any> {
    if (this.selectedTabValue === 'rewardsHistory') {
      return this.rewardsHistoryTemplate;
    } else if (this.selectedTabValue === 'itemHistory') {
      return this.itemHistoryTemplate;
    } else {
      return this.rewardsTemplate;
    }
  }

  initIsMobile(): void {
    this.isMobile = this.media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, true],
      [MediaBreakpoints.TABLET_LANDSCAPE, false],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }

  onTabChanged(event: MatTabChangeEvent) {
    if (this.selectedTabValue === 'rewards' && this.selectedTab && event.tab.textLabel === this.selectedTab.label) {
      this.changedToRewardTab = true;
      this.changedToRewardHistoryTab = false;
      this.changedToItemHistoryTab = false;
    } else if (this.selectedTabValue === 'rewardsHistory' && this.selectedTab && event.tab.textLabel === this.selectedTab.label) {
      this.changedToRewardHistoryTab = true;
      this.changedToRewardTab = false;
      this.changedToItemHistoryTab = false;
    } else if (this.selectedTabValue === 'itemHistory' && this.selectedTab && event.tab.textLabel === this.selectedTab.label) {
      this.changedToItemHistoryTab = true;
      this.changedToRewardTab = false;
      this.changedToRewardHistoryTab = false;
    }
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
