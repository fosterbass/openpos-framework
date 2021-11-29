
import { TimeUnitLabels } from '../../utilites/time-utils';
import { IOrderCustomer } from './order-customer.interface';

export interface IOrderSummary {
    number: string;
    title: string;
    customer: IOrderCustomer;
    priorityFlag: boolean;
    orderType: string;
    orderTypeIcon: string;
    orderTypeLabel: string;
    itemCount: string;
    itemCountLabel: string;
    itemCountIcon: string;
    statusCode: string;
    status: string;
    statusLabel: string;
    statusIcon: string;
    orderDue: Date;
    orderDueLabel: string;
    orderDueIcon: string;
    orderCreated: Date;
    orderCreatedLabel: string;
    orderTotal: string;
    orderTotalLabel: string;
    amountDue: string;
    amountDueLabel: string;
    paymentStatus: string;
    timeUnitLabels: TimeUnitLabels;
}
