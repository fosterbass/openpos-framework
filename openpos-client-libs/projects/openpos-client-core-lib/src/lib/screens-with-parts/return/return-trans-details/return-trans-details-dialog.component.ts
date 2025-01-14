import { CONFIGURATION } from '../../../configuration/configuration';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { SelectionMode } from '../../../core/interfaces/selection-mode.enum';
import { ISellItem } from '../../../core/interfaces/sell-item.interface';
import { Component } from '@angular/core';
import { DialogComponent } from '../../../shared/decorators/dialog-component.decorator';
import { SelectableItemListComponentConfiguration } from '../../../shared/components/selectable-item-list/selectable-item-list.component';
import { ReturnTransDetailsInterface } from './return-trans-detals.interface';
import { PosScreenDirective } from '../../pos-screen/pos-screen.component';
import { Observable } from 'rxjs';
import { ISelectableListData } from '../../../shared/components/selectable-item-list/selectable-list-data.interface';
import { ICheckboxField } from '../../../core/interfaces/form-field.interface';

@DialogComponent({
    name: 'ReturnTransDetailDialog'
})
@Component({
    selector: 'app-return-trans-details-dialog',
    templateUrl: './return-trans-details-dialog.component.html',
    styleUrls: ['./return-trans-details-dialog.component.scss']
})
export class ReturnTransDetailsDialogComponent extends PosScreenDirective<ReturnTransDetailsInterface>  {
    listData: Observable<ISelectableListData<ISellItem>>;
    listConfig: SelectableItemListComponentConfiguration;
    selectionButton: IActionItem;
    additionalButtons: IActionItem[];
    employeeTransaction: ICheckboxField;
    index = -1;

    public onItemChange(event: any): void {
        this.index = event;
    }

    buildScreen() {
        const allItems = new Map<number, ISellItem>();
        const allDisabledItems = new Map<number, ISellItem>();
        for (let i = 0; i < this.screen.items.length; i++) {
            const item = this.screen.items[i];
            allItems.set(i, item);
            if (!item.enabled) {
                allDisabledItems.set(i, item);
            }
        }

        this.listData = new Observable<ISelectableListData<ISellItem>>((observer) => {
            observer.next({
                items: allItems,
                disabledItems: allDisabledItems,
            } as ISelectableListData<ISellItem>);
        });

        this.selectionButton = this.screen.selectionButton;
        this.additionalButtons = this.screen.additionalButtons;
        this.employeeTransaction = this.screen.employeeTransaction;
        this.listConfig = new SelectableItemListComponentConfiguration();
        this.listConfig.selectionMode = SelectionMode.Single;
        this.listConfig.numItemsPerPage = Number.MAX_VALUE;
        this.listConfig.totalNumberOfItems = this.screen.items.length;
    }

    public doMenuItemAction(menuItem: IActionItem) {
        if (this.index > -1) {
            this.doAction(menuItem, this.index);
        }
    }

    public onEmployeeTransaction($event) {
        this.doAction('MarkAsEmployeeTransaction', $event.checked);
    }

    public keybindsEnabled(menuItem: IActionItem): boolean {
        return CONFIGURATION.enableKeybinds && menuItem.keybind && menuItem.keybind !== 'Enter';
    }
}
