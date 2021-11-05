import {ActionItem} from '../../core/actions/action-item';
import { Plan } from './plan-interface';
import { EnrollmentItem } from './enrollment-item-interface';

export class SubscriptionAccount {
    iconImageUrl: string;
    iconText: string;
    copy: string;
    enrollmentItems: EnrollmentItem[];
    listTitle: string;
    plans: Plan[];
    signupActionItem: ActionItem;
}
