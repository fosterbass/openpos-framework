import {
    Component,
    EventEmitter,
    Input,
    Output,
    ElementRef,
    QueryList,
    ViewChildren,
    TemplateRef,
    ContentChild
} from '@angular/core';
@Component({
    selector: 'app-radio-item-list',
    templateUrl: './radio-item-list.component.html'
})
export class RadioItemListComponent<ItemType> {
    @ContentChild(TemplateRef) itemTemplate: TemplateRef<ElementRef>;

    @Input()
    items: ItemType[];

    @Output()
    itemClick: EventEmitter<ItemType> = new EventEmitter<any>();

    onItemClick(item: ItemType): void {
         this.itemClick.emit(item);
    }
}
