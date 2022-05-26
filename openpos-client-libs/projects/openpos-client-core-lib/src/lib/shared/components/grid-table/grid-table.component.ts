import { Component, Input } from '@angular/core';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { ActionService } from '../../../core/actions/action.service';

@Component({
    selector: 'app-grid-table',
    templateUrl: './grid-table.component.html',
    styleUrls: ['./grid-table.component.scss']
})
export class GridTableComponent {

    @Input()
    columnHeaders = [];

    @Input()
    rows = [];

    @Input()
    shadeAlternating = false;

    constructor(public actionService: ActionService) {
    }

    isAction(line: any): boolean {
        return line instanceof Object && line.hasOwnProperty('action');
    }

    public doAction(action: IActionItem) {
        this.actionService.doAction(action);
    }

}
