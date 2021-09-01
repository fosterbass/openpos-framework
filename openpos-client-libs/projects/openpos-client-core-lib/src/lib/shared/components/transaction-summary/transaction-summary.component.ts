import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ITransactionSummary} from './transaction-summary.interface';
import {IActionItem} from '../../../core/actions/action-item.interface';
import {ActionService} from '../../../core/actions/action.service';
import {TransactionService} from '../../../core/services/transaction.service';
import {TransTypeEnum} from '../../trans-type.enum';
import {Observable} from "rxjs";
import {MediaBreakpoints, OpenposMediaService} from "../../../core/media/openpos-media.service";

@Component({
  selector: 'app-transaction-summary',
  templateUrl: './transaction-summary.component.html',
  styleUrls: ['./transaction-summary.component.scss']
})
export class TransactionSummaryComponent implements OnChanges {
  @Input()
  transactionSummary: ITransactionSummary;

  statusClass: string;
  TransTypeEnum = TransTypeEnum;
  isMobile$: Observable<boolean>;

  constructor(private actionService: ActionService, private transactionService: TransactionService,
              media: OpenposMediaService) {
    this.isMobile$ = media.observe(new Map([
      [MediaBreakpoints.MOBILE_PORTRAIT, true],
      [MediaBreakpoints.MOBILE_LANDSCAPE, true],
      [MediaBreakpoints.TABLET_PORTRAIT, true],
      [MediaBreakpoints.TABLET_LANDSCAPE, true],
      [MediaBreakpoints.DESKTOP_PORTRAIT, false],
      [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
    ]));
  }

  onClick(actionItem: IActionItem): void {
    this.actionService.doAction(actionItem, this.transactionSummary);
  }

  ngOnChanges(changes: SimpleChanges) {
    this.statusClass = this.transactionService.mapStatusToCssClass(this.transactionSummary.status);
  }
}
