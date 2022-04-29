import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { IActionItem } from '../../../core/actions/action-item.interface';
import {Membership} from '../membership-display/memebership-display.interface';

export interface MobileLoyaltyPartInterface extends IAbstractScreen {
    mobileLoyaltyButton: IActionItem;
    loyaltyButton: IActionItem;
    customer: { name: string, label: string, icon: string, id: string };
    mobileLoyaltySaleShowMembershipsHideLogo: boolean;
    memberships: Membership[];
    noMembershipsFoundLabel: string;
    alertMessage: string;
}
