import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { ITab } from '../../../shared/components/tabbed-content-card/tab.interface';
import { SubscriptionAccount } from '../subscription-account-interface';

export interface MembershipDetailsDialogInterface extends IAbstractScreen {
    tabs: ITab[];
    subscriptionAccounts: SubscriptionAccount[];
}
