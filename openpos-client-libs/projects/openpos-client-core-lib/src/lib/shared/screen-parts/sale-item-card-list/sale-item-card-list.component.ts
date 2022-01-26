import type { QueryList } from '@angular/core';
import { AfterViewInit, Component, ElementRef, EventEmitter, Injector, Output, ViewChildren } from '@angular/core';
import { SaleItemCardListInterface } from './sale-item-card-list.interface';
import { ScreenPart } from '../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../screen-part';
import { UIDataMessageService } from '../../../core/ui-data-message/ui-data-message.service';
import { merge, Observable } from 'rxjs';
import { ISellItem } from '../../../core/interfaces/sell-item.interface';
import { filter, takeUntil } from 'rxjs/operators';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';

@ScreenPart({
  name: 'SaleItemCardList'
})
@Component({
  selector: 'app-sale-item-card-list',
  templateUrl: './sale-item-card-list.component.html',
  styleUrls: ['./sale-item-card-list.component.scss']
})
export class SaleItemCardListComponent extends ScreenPartComponent<SaleItemCardListInterface> implements AfterViewInit {
  stop$: Observable<any>;
  expandedIndex = -1;
  numItems = 0;
  items$: Observable<ISellItem[]>;
  previousSellItems: ISellItem[];
  @ViewChildren('items', { read: ElementRef }) private itemsRef: QueryList<ElementRef>;
  @Output() itemsChanged = new EventEmitter<ISellItem[]>();

  constructor(injector: Injector,
              private dataMessageService: UIDataMessageService,
              protected keybindingZoneService: KeybindingZoneService) {
    super(injector);
    this.stop$ = merge(this.beforeScreenDataUpdated$, this.destroyed$);

    this.keybindingZoneService.getKeyDownEvent('ArrowUp,ArrowDown,Tab')
        .pipe(
            filter(event => !event.domEvent.repeat),
            takeUntil(this.destroyed$)
        ).subscribe(event => this.handleArrowKey(event.domEvent));
  }

  itemsTrackByFn(index, item: ISellItem) {
    return item.index;
  }

  screenDataUpdated() {
    this.items$ = this.dataMessageService.getData$(this.screenData.providerKey);
    this.items$.pipe(
      takeUntil(this.stop$)
    ).subscribe(sellItems => this.onSellItemsChange(sellItems));
  }

  onSellItemsChange(sellItems: ISellItem[]): void {
    this.keybindingZoneService.removeAllKeybindings(this.previousSellItems);
    this.keybindingZoneService.addAllKeybindings(sellItems);
    this.previousSellItems = sellItems;

    this.items$.forEach(items => {
      this.numItems = items.length;
      this.itemsChanged.emit(items);
      this.expandedIndex = items.length - 1;
    });
    this.scrollToView(this.expandedIndex);
  }

  ngAfterViewInit() {
    this.scrollToView(this.expandedIndex);
  }

  scrollToView(index: number): void {
    if (this.itemsRef) {
      const itemsRefArray = this.itemsRef.toArray();
      if (itemsRefArray && index >= 0 && index < itemsRefArray.length) {
        itemsRefArray[index].nativeElement.scrollIntoView({ block: 'center' });
      }
    }
  }

  isItemExpanded(index: number): boolean {
    if (this.screenData.enableCollapsibleItems) {
      return index === this.expandedIndex;
    }
    return true;
  }

  updateExpandedIndex(index: number) {
    this.expandedIndex = index;
    this.scrollToView(this.expandedIndex);
  }

  handleArrowKey(event: KeyboardEvent) {
    let direction = 1;
    if (event.key === 'ArrowDown' || event.key === 'Tab') {
      direction = 1;
    } else if (event.key === 'ArrowUp') {
      direction = -1;
    } else {
      return;
    }

    let newIndex = this.expandedIndex + direction;

    if (this.expandedIndex === this.numItems - 1 && event.key === 'Tab') {
      newIndex = 0;
    }

    if (newIndex >= 0 && newIndex < this.numItems) {
      this.updateExpandedIndex(newIndex);
    }
  }

}
