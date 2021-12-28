import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { CustomerDetails } from '../../../shared/screen-parts/customer-information/customer-information.interface';

export interface CustomerDetailsDialogInterface extends IAbstractScreen {
    message: string;
    customer: CustomerDetails;
    membershipEnabled: boolean;
    membershipPointsEnabled: boolean;
    membershipLabel: string;
    loyaltyPromotions: IActionItem;
    secondaryButtons: IActionItem[];
    doneButton: IActionItem;
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
    profileIcon: string;
    membershipCardIcon: string;
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
