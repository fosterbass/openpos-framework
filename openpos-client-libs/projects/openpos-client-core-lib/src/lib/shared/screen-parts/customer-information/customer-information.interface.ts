import { Membership } from '../membership-display/memebership-display.interface';
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
    phoneNumberType: string;
    email: string;
    emailType: string;
    address: {
        line1: string,
        line2: string,
        city: string,
        state: string,
        postalCode: string,
        type: string
    };
    memberships: Membership[];
    itemHistory: PurchasedItem[];
    numberOfActiveRewards: number;
    numberOfHistoricRewards: number;
    birthDate: string;
    memberTier: string;
    membershipSignUpAction: IActionItem;
    enrolledMembershipAction: IActionItem;
}
export interface CustomerInformationComponentInterface {
    emailIcon: string;
    phoneIcon: string;
    loyaltyNumberIcon: string;
    locationIcon: string;
    birthDateIcon: string;
    addressLines: string[];
}
