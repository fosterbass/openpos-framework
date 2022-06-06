import { IAbstractScreen } from '../../core/interfaces/abstract-screen.interface';
import { IActionItem } from '../../core/actions/action-item.interface';
import { CustomerDetails } from '../../shared/screen-parts/customer-information/customer-information.interface';
import { ITab } from '../../shared/components/tabbed-content-card/tab.interface';

export interface CustomerDetailsDialogInterface extends IAbstractScreen {
    membershipSignUpAction: any;
    rewardsDataKey: string;
    rewardsHistoryDataKey: string;
    customer: CustomerDetails;
    membershipEnabled: boolean;
    membershipPointsEnabled: boolean;
    membershipLabel: string;
    appliedLabel: string;
    backButton: IActionItem;
    secondaryButtons: IActionItem[];
    doneButton: IActionItem;
    contactLabel: string;
    memberTierLabel: string;
    noPromotionsLabel: string;
    noMembershipsFoundLabel: string;
    itemHistoryFilterLabel: string;
    appliedIcon: string;
    applyIcon: string;
    backIcon: string;
    membershipCardIcon: string;
    profileIcon: string;
    phoneIcon: string;
    emailIcon: string;
    locationIcon: string;
    loyaltyNumberIcon: string;
    itemsHistoryDataProviderKey: string;
    itemHistoryFilter: CustomerItemHistoryFilter;
    tabs: ITab[];
}

export interface CustomerItemHistoryFilter {
    fromDatePlaceholder: string;
    toDatePlaceholder: string;
    textPlaceholder: string;

    fromDate: string;
    toDate: string;
    text: string;
}
