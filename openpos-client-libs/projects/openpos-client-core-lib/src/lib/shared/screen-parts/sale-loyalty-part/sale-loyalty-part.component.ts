
import { glowContractTrigger, glowPulseTrigger } from '../../animations/glow.animation';
import { shakeTrigger } from '../../animations/shake.animation';
import { throbTrigger } from '../../animations/throb.animation';
import { swingTrigger } from '../../animations/swing.animation';
import { gradientInnerGlowTrigger } from '../../animations/gradient-inner-glow.animation';
import { ScreenPart } from '../../decorators/screen-part.decorator';
import { Component, Injector } from '@angular/core';
import { ScreenPartComponent } from '../screen-part';
import { Observable } from 'rxjs';
import { MediaBreakpoints, OpenposMediaService } from '../../../core/media/openpos-media.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { Reward } from '../rewards-line-item/rewards-line-item.interface';
import { SaleLoyaltyPartInterface } from './sale-loyalty-part.interface';
import {LoyaltySalePartService} from '../../../core/services/loyalty-sale-part.service';

@ScreenPart({
    name: 'SaleLoyalty'
})
@Component({
    selector: 'app-sale-loyalty-part',
    templateUrl: './sale-loyalty-part.component.html',
    styleUrls: ['./sale-loyalty-part.component.scss'],
    animations: [
        glowContractTrigger,
        glowPulseTrigger,
        shakeTrigger,
        throbTrigger,
        swingTrigger,
        gradientInnerGlowTrigger
    ]
})
export class SaleLoyaltyPartComponent extends ScreenPartComponent<SaleLoyaltyPartInterface> {

    private loyaltyIconToken = '${icon}';
    public loyaltyBefore: string;
    public loyaltyAfter: string;
    public isLoyaltyOperationInProgressOnCustomerDisplay$: Observable<boolean>;
    public loyaltyOperationInProgressDetailsMessage$: Observable<string>;
    public glowPulseRepeatTrigger = true;
    public gradientInnerGlowRepeatTrigger = true;

    constructor(injector: Injector, media: OpenposMediaService, private loyaltySalePartService: LoyaltySalePartService) {
        super(injector);
        this.isMobile$ = media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, false],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));

        this.isLoyaltyOperationInProgressOnCustomerDisplay$ = this.loyaltySalePartService.isActiveOnCustomerDisplay();
        this.loyaltyOperationInProgressDetailsMessage$ = this.loyaltySalePartService.getCustomerDisplayDetailsMessage();
        this.loyaltySalePartService.checkCustomerDisplayStatus();
    }

    screenDataUpdated() {
        if (this.screenData.loyaltyButton) {
            const title = this.screenData.loyaltyButton.title;
            const parts = title.split(this.loyaltyIconToken);
            if (parts && parts.length > 0) {
                this.loyaltyBefore = parts[0].trim();
                if (parts.length > 1) {
                    this.loyaltyAfter = parts[1].trim();
                }
            }
        }
    }


    public keybindsEnabled(menuItem: IActionItem): boolean {
        return CONFIGURATION.enableKeybinds && !!menuItem.keybind && menuItem.keybind !== 'Enter';
    }

    public mapToSimpleReward(reward: Reward): any {
        // Only show some values because screen space is limited
        return {
            name: reward.name,
            reward: reward.reward
        };
    }

    public isMissingCustomerInfo(): boolean {
        return this.screenData.customerMissingInfoEnabled && this.screenData.customerMissingInfo;
    }

    public repeatGlowPulse(): void {
        this.glowPulseRepeatTrigger = !this.glowPulseRepeatTrigger;
    }

    public repeatGradientInnerGlow(): void {
        this.gradientInnerGlowRepeatTrigger = !this.gradientInnerGlowRepeatTrigger;
    }

    public shouldShowLookupCustomer(): boolean {
        return !this.screenData.readOnly
            && !!this.screenData.loyaltyButton
            && (!this.screenData.customer || !this.screenData.customer.id);
    }

    public shouldShowLinkedCustomer(): boolean {
        return !this.screenData.readOnly
            && !!this.screenData.loyaltyButton
            && !!this.screenData.customer
            && !!this.screenData.customer.id;
    }

    public shouldShowLoyaltyOperationInProgress(): boolean {
        return !this.screenData.readOnly;
    }
}
