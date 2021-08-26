import {Membership} from '../membership-display/memebership-display.interface';
import {Reward} from '../rewards-line-item/rewards-line-item.interface';
import {RewardHistory} from '../rewards-history-line-item/rewards-history-line-item.interface';
import { IActionItem } from '../../../core/actions/action-item.interface';

export interface CustomerDetails {
    name: string;
    loyaltyNumber: string;
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
}
export interface CustomerInformationComponentInterface {
    emailIcon: string;
    phoneIcon: string;
    loyaltyNumberIcon: string;
    locationIcon: string;
    birthDateIcon: string;
}

export interface PurchasedItem {
    title: string;
    salePrice: string;
    originalPrice: string;
    imageUrl: string;
    labels: UILabel[];
    transaction: TransactionIdentifier;
    transactionDetailsAction: IActionItem;
    itemId: string;
    itemDetailsAction: IActionItem;
}

export interface TransactionIdentifier {
    sequenceNumber: number;
    deviceId: string;
    businessDate: string;
    voidedSequenceNumber?: number;
}

export interface UILabel {
    icon: string;
    text: string;
}
