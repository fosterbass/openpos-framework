import { Component, ViewChild, ElementRef, Input, Output, EventEmitter, AfterViewChecked } from '@angular/core';
import { ISellItem } from '../../../core/interfaces/sell-item.interface';
import { SelectableItemListComponentConfiguration } from '../selectable-item-list/selectable-item-list.component';
import { CONFIGURATION } from '../../../configuration/configuration';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { IActionItemGroup } from '../../../core/actions/action-item-group.interface';
import { IItem } from '../../../core/interfaces/item.interface';
import { Observable } from 'rxjs';
import { ISelectableListData } from '../selectable-item-list/selectable-list-data.interface';
import { KebabButtonComponent } from '../kebab-button/kebab-button.component';


@Component({
  selector: 'app-transaction-item-list',
  templateUrl: './transaction-item-list.component.html',
  styleUrls: ['./transaction-item-list.component.scss']
})
export class TransactionItemListComponent implements AfterViewChecked {

  @ViewChild('scrollList', { read: ElementRef, static: true }) private scrollList: ElementRef;
  @ViewChild('txMenu') private txMenuButton: KebabButtonComponent;

  @Input() listData: Observable<ISelectableListData<ISellItem>>;
  @Input() listConfig: SelectableItemListComponentConfiguration;
  @Input() selectedItems: ISellItem[];
  @Input() selectedItemIndexes: number[];
  @Input() multiSelectedMenuItems: IActionItem[];
  @Input() transactionMenu: IActionItemGroup;
  @Input() prompt: string;
  @Input() readOnly: boolean;

  @Output() selectedItemListChange = new EventEmitter<number[]>();
  @Output() menuAction = new EventEmitter<any>();

  individualMenuClicked = false;

  size = -1;

  public onItemListChange(event: number[]): void {
    this.selectedItemIndexes = event;
    if (this.individualMenuClicked) {
      this.individualMenuClicked = false;
      return;
    }
    this.selectedItemListChange.emit(event);
  }

  public onItemActionsMultiMenulick(menuItem: IActionItem, selectedItemIndexes: number[], selectedItems: IItem[]) {
    if (menuItem.enabled && selectedItems && selectedItems.length > 0) {
      this.menuAction.emit({ menuItem, payload: selectedItems.map(i => i.index) });
    } else {
      this.onMenuItemClick(menuItem, selectedItemIndexes);
    }
  }

  public onMenuItemClick(menuItem: IActionItem, payload?: number[]) {
    if (menuItem.enabled && payload) {
      this.menuAction.emit({ menuItem, payload });
    } else if (menuItem.enabled) {
      this.menuAction.emit(menuItem);
    }
  }

  public menuItemLabel(): string {
    return `${this.selectedItems.length} Item${this.selectedItems.length === 1 ? '' : 's'}  Selected`;
  }

  ngAfterViewChecked() {
    if (this.listConfig && this.listConfig.totalNumberOfItems && this.size !== this.listConfig.totalNumberOfItems) {
      this.scrollToBottom();
      this.size = this.listConfig.totalNumberOfItems;
    }
  }

  scrollToBottom(): void {
    try {
      this.scrollList.nativeElement.scrollTop = this.scrollList.nativeElement.scrollHeight;
    } catch (err) { }
  }

  public keybindsEnabled() {
    return CONFIGURATION.enableKeybinds;
  }

  public openTxMenu() {
    this.txMenuButton.openKebabMenu();
  }

}
