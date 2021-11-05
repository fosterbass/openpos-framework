import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { SubscriptionAccount } from '../subscription-account-interface';

export interface MembershipDetailsDialogInterface extends IAbstractScreen {
    subscriptionAccounts: SubscriptionAccount[];
}
