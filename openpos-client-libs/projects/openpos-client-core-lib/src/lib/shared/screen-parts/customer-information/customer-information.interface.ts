import { Membership } from '../membership-display/memebership-display.interface';
import { Reward } from '../rewards-line-item/rewards-line-item.interface';
import { RewardHistory } from '../rewards-history-line-item/rewards-history-line-item.interface';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { PurchasedItem } from '../purchase-history-item/purchase-history-item.interface';

export interface CustomerDetails {
    name: string;
    loyaltyNumber: string;
    accountNumberLabel: string;
    accountNumber: string;
    creditLimitLabel: string;
    creditLimit: string;
    expiryDateLabel: string;
    expiryDate: string;
    phoneNumber: string;
    email: string;
    address: {
        line1: string,
        line2: string,
        city: string,
        state: string,
        postalCode: string
    };
    memberships: Membership[];
    rewards: Reward[];
    rewardHistory: RewardHistory[];
    itemHistory: PurchasedItem[];
    birthDate: string;
    memberTier: string;
}

export interface CustomerInformationComponentInterface {
    emailIcon: string;
    phoneIcon: string;
    loyaltyNumberIcon: string;
    locationIcon: string;
    birthDateIcon: string;
}
