import { Component, Injector, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { PurchasedItem } from '../customer-information/customer-information.interface';
import { ScreenPartComponent } from '../screen-part';

@Component({
    selector: 'app-purchase-history-item',
    templateUrl: './purchase-history-item.component.html',
    styleUrls: ['./purchase-history-item.component.scss']})
export class PurchaseHistoryItemComponent extends ScreenPartComponent<PurchasedItem> {

    @Input()
    item: PurchasedItem;

    isMobile: Observable<boolean>;

    constructor(injector: Injector, private media: OpenposMediaService) {
        super(injector);
        this.initIsMobile();
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

    screenDataUpdated() {
    }
}
