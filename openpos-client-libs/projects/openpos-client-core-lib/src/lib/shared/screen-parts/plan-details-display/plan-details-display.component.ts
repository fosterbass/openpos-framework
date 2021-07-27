import {Component, Injector, Input, OnInit} from '@angular/core';
import {MediaBreakpoints, OpenposMediaService} from "../../../core/media/openpos-media.service";
import {Observable} from "rxjs";
import {Plan} from "../../../screens-with-parts/sale/program-interface";
import {SafeHtml} from "@angular/platform-browser/src/security/dom_sanitization_service";
import {DomSanitizer} from "@angular/platform-browser";
import {ActionService} from "../../../core/actions/action.service";
import {IActionItem} from "../../../core/actions/action-item.interface";

@Component({
    selector: 'app-plan-details-display',
    templateUrl: './plan-details-display.component.html',
    styleUrls: ['./plan-details-display.component.scss']})
export class PlanDetailsDisplayComponent implements OnInit {
    @Input()
    plan: Plan;
    safeCopy: SafeHtml;
    isMobile: Observable<boolean>;

    constructor(injector: Injector, private media: OpenposMediaService, private sanitizer: DomSanitizer, private actionService: ActionService) {
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

    ngOnInit() {
        this.safeCopy =  this.sanitizer.bypassSecurityTrustHtml(this.plan.copy);
    }

    doAction( action: IActionItem ) {
        this.actionService.doAction(action);
    }
}
