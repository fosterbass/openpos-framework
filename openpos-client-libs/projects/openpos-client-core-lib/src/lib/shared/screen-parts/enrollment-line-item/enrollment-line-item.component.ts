import { Component, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { ActionService } from '../../../core/actions/action.service';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { EnrollmentItem } from '../../../screens-with-parts/membership/enrollment-item-interface';

@Component({
    selector: 'app-enrollment-line-item',
    templateUrl: './enrollment-line-item.component.html',
    styleUrls: ['./enrollment-line-item.component.scss']
})
export class EnrollmentLineItemComponent {
    @Input()
    enrollment: EnrollmentItem;
    isMobile: Observable<boolean>;

    constructor(private media: OpenposMediaService, public actionService: ActionService) {
        this.initIsMobile();
    }

    initIsMobile(): void {
        this.isMobile = this.media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, false],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));
    }

    doAction(action: IActionItem | string, payload?: any) {
        if (typeof (action) === 'string') {
            this.actionService.doAction({ action }, payload);
        } else {
            this.actionService.doAction(action, payload);
        }
    }
}
