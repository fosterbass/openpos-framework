import {ActionItem} from '../../core/actions/action-item';

export class EnrollmentItemProperty {
    name: string;
    value: string;
    type: string;
    icon: string;
}

export class EnrollmentItem {
    title: EnrollmentItemProperty;
    icon: string;
    details: EnrollmentItemProperty[];
    actionItemList: ActionItem[];
}

