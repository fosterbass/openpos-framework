import { Component, Injector } from '@angular/core';
import { MembershipPlanDetailsDialogInterface } from './membership-plan-details-dialog.interface';
import { DialogComponent } from '../../../shared/decorators/dialog-component.decorator';
import { PosScreenDirective } from '../../pos-screen/pos-screen.component';
import { Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { ActionService } from '../../../core/actions/action.service';

@DialogComponent({
    name: 'MembershipPlanDetailsDialog'
})
@Component({
    selector: 'app-membership-plan-details-dialog',
    templateUrl: './membership-plan-details-dialog.component.html',
    styleUrls: ['./membership-plan-details-dialog.component.scss']
})
export class MembershipPlanDetailsDialogComponent extends PosScreenDirective<MembershipPlanDetailsDialogInterface>{

    isMobile: Observable<boolean>;
    constructor(public actionService: ActionService, injector: Injector, private media: OpenposMediaService) {
        super(injector);
        this.initIsMobile();
    }

    initIsMobile(): void {
        this.isMobile = this.media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, true],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));
    }

    buildScreen() {
    }

    public keybindsEnabled(menuItem: IActionItem): boolean {
        return CONFIGURATION.enableKeybinds && !!menuItem.keybind && menuItem.keybind !== 'Enter';
    }
}
