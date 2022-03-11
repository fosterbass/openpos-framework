import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatDialog } from '@angular/material/dialog';
import { Component, Injector } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { IActionItem } from '../../core/actions/action-item.interface';
import { ITransactionReceipt } from '../../shared/components/receipt-card/transaction-receipt.interface';
import { OpenposMediaService, MediaBreakpoints } from '../../core/media/openpos-media.service';
import { MobileReturnReceiptsSheetComponent } from './mobile-return-receipts-sheet/mobile-return-receipts-sheet.component';

/**
 * @ignore
 */
@ScreenComponent({
    name: 'Return'
})
@Component({
    selector: 'app-return',
    templateUrl: './return.component.html',
    styleUrls: ['./return.component.scss']
})
export class ReturnComponent extends PosScreenDirective<any> {

    isMobile: Observable<boolean>;

    public size = -1;
    individualMenuClicked = false;

    public overFlowListSize: Observable<number>;

    public itemTotal: number;
    public receipts: ITransactionReceipt[];
    public removeReceiptAction: IActionItem;

    constructor(protected dialog: MatDialog, injector: Injector,
                media: OpenposMediaService, private bottomSheet: MatBottomSheet ) {
        super(injector);
        this.isMobile = media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));
    }

    buildScreen() {
        this.receipts = this.screen.receipts;
        this.removeReceiptAction = this.screen.removeReceiptAction;
        this.dialog.closeAll();
    }

    public onReceiptClick(event: any) {
        if (this.receipts) {
            const index = this.receipts.indexOf(event);
            this.doAction('TransactionDetails', index);
        }
    }

    openSheet(): void {
        console.log('Entering openSheet()');
        const ref = this.bottomSheet.open( MobileReturnReceiptsSheetComponent,
            {data: this.screen, panelClass: 'sheet'});
        this.subscriptions.add(new Subscription( () => ref.dismiss()));
        this.subscriptions.add(ref.afterDismissed().subscribe( item => {
            if (item !== undefined && item !== null) {
                if (typeof item === 'object') {
                    this.doAction(this.removeReceiptAction, item.barcode);
                } else if (typeof item === 'number') {
                    this.doAction('TransactionDetails', item);
                }
            }
        }));
    }

}
