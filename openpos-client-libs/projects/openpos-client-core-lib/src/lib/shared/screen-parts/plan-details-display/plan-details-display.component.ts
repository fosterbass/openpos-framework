import { Component, Input } from '@angular/core';
import { Plan } from '../../../screens-with-parts/membership/plan-interface';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { Observable } from 'rxjs';
import { ActionService } from '../../../core/actions/action.service';
import { IActionItem } from '../../../core/actions/action-item.interface';

@Component({
    selector: 'app-plan-details-display',
    templateUrl: './plan-details-display.component.html',
    styleUrls: ['./plan-details-display.component.scss']
})
export class PlanDetailsDisplayComponent {
    @Input()
    plan: Plan;
    isMobile: Observable<boolean>;

    constructor(private media: OpenposMediaService,
                private actionService: ActionService) {
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

    doAction(action: IActionItem) {
        this.actionService.doAction(action);
    }
}
