import { IActionItem } from '../../core/actions/action-item.interface';
import { IAbstractScreen } from '../../core/interfaces/abstract-screen.interface';
import { CustomerDetails } from '../../shared/screen-parts/customer-information/customer-information.interface';

export interface CustomerDetailsDialogInterface extends IAbstractScreen {
    membershipSignUpAction: any;
    message: string;
    rewardsDataKey: string;
    rewardsHistoryDataKey: string;
    customer: CustomerDetails;
    membershipEnabled: boolean;
    membershipPointsEnabled: boolean;
    membershipLabel: string;
    loyaltyPromotions: IActionItem;
    secondaryButtons: IActionItem[];
    doneButton: IActionItem;
    additionalActions: IActionItem[];
    contactLabel: string;
    plccAccountDetailsLabel: string;
    rewardsLabel: string;
    rewardTabEnabled: boolean;
    rewardHistoryLabel: string;
    rewardHistoryTabEnabled: boolean;
    memberTierLabel: string;
    noPromotionsLabel: string;
    noMembershipsFoundLabel: string;
    itemHistoryEnabled: boolean;
    itemHistoryLabel: string;
    itemHistoryFilterLabel: string;
    appliedIcon: string;
    applyIcon: string;
    backIcon: string;
    membershipCardIcon: string;
    profileIcon: string;
    statusIcon: string;
    phoneIcon: string;
    emailIcon: string;
    locationIcon: string;
    loyaltyNumberIcon: string;
    rewardHistoryIcon: string;
    itemHistoryIcon: string;
    itemsHistoryDataProviderKey: string;
    itemHistoryFilter: CustomerItemHistoryFilter;
}

export interface CustomerItemHistoryFilter {
    fromDatePlaceholder: string;
    toDatePlaceholder: string;
    textPlaceholder: string;

    fromDate: string;
    toDate: string;
    text: string;
}
