import { Component, Injector } from '@angular/core';
import { SaleTotalPanelInterface } from './sale-total-panel.interface';
import { ScreenPart } from '../../decorators/screen-part.decorator';
import { ScreenPartComponent } from '../screen-part';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { LoyaltySalePartService } from '../../../core/services/loyalty-sale-part.service';
import { Observable } from 'rxjs';
@ScreenPart({
    name: 'SaleTotalPanel'
})
@Component({
    selector: 'app-sale-total-panel',
    templateUrl: './sale-total-panel.component.html',
    styleUrls: ['./sale-total-panel.component.scss']
})
export class SaleTotalPanelComponent extends ScreenPartComponent<SaleTotalPanelInterface> {

    public isLoyaltyOperationInProgressOnCustomerDisplay$: Observable<boolean>;

    constructor(injector: Injector, media: OpenposMediaService, private loyaltySignupService: LoyaltySalePartService) {
        super(injector);
        this.isMobile$ = media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));

        this.isLoyaltyOperationInProgressOnCustomerDisplay$ = this.loyaltySignupService.isActiveOnCustomerDisplay();
        this.loyaltySignupService.checkCustomerDisplayStatus();
    }

    screenDataUpdated() {
    }

    public keybindsEnabled(menuItem: IActionItem): boolean {
        return CONFIGURATION.enableKeybinds && !!menuItem.keybind && menuItem.keybind !== 'Enter';
    }

    public doMenuItemAction(menuItem: IActionItem): void {
        this.doAction(menuItem);
    }

    public shouldShowHeader(): boolean {
        return this.shouldShowLookupCustomer()
            || this.shouldShowLinkedCustomer();
    }

    private shouldShowLookupCustomer(): boolean {
        return !this.screenData.readOnly
            && !!this.screenData.loyaltyButton
            && !this.screenData.customer;
    }

    private shouldShowLinkedCustomer(): boolean {
        return !this.screenData.readOnly
            && !!this.screenData.loyaltyButton
            && !!this.screenData.customer;
    }

}
