
import { Component } from '@angular/core';
import { PosScreen } from '../pos-screen/pos-screen.component';
import { IActionItem } from '../../core/interfaces/menu-item.interface';

@Component({
  selector: 'app-till-summary',
  templateUrl: './till-summary.component.html',
  styleUrls: ['./till-summary.component.scss']
})
export class TillSummaryComponent extends PosScreen<any> {

    nextAction: IActionItem;

    buildScreen() {
        this.nextAction = this.screen.nextAction;
    }

    onNextAction() {
        this.session.onAction(this.nextAction.action);
    }

}
