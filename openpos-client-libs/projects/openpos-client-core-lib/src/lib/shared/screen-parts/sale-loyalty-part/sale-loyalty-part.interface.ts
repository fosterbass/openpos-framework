import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { Membership } from '../membership-display/memebership-display.interface';
import { Reward } from '../rewards-line-item/rewards-line-item.interface';

export interface SaleLoyaltyPartInterface extends IAbstractScreen {
    loyaltyButton: IActionItem;
    customer: { name: string, label: string, icon: string, id: string };
    readOnly: boolean;
    profileIcon: string;
    loyaltyIDLabel: string;
    noMembershipsFoundLabel: string;
    membershipEnabled: boolean;
    memberships: Membership[];
    showCustomerDataWhenMissingId: boolean;
    customerMissingInfoEnabled: boolean;
    customerMissingInfo: boolean;
    customerMissingInfoIcon: string;
    customerMissingInfoLabel: string;
    loyaltyOperationInProgressTitle: string;
    loyaltyOperationInProgressIcon: string;
    loyaltyOperationInProgressDetailsIcon: string;
    loyaltyCancelButton: IActionItem;
    membershipVisibleOnLinkButton: boolean;
    rewardsVisibleOnLinkButton: boolean;
    customerEmail: string;
    memberTierLabel: string;
    rewardsLabel: string;
    memberTier: string;
    loyaltyIcon: string;
    loyaltyRewards: Reward[];
    noPromotionsLabel: string;
    alertMessage: string;
}
