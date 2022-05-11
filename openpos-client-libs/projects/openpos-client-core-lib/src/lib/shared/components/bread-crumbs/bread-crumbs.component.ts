import { Component, Input } from '@angular/core';
import { ActionService } from '../../../core/actions/action.service';
import { IActionItem } from '../../../core/actions/action-item.interface';

@Component({
    selector: 'app-bread-crumbs',
    templateUrl: './bread-crumbs.component.html',
    styleUrls: ['./bread-crumbs.component.scss']
})
export class BreadCrumbsComponent {

    @Input()
    crumbs: IActionItem[];

    constructor(private actionService: ActionService) { }

    doAction(action: IActionItem) {
        this.actionService.doAction(action);
    }
}
