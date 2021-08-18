import {IActionItem} from '../../../core/actions/action-item.interface';

export interface PurchasedItem {
    title: string;
    labels: UILabel[];
    imageUrl: string;
    salePrice: string;
    originalPrice: string;
    transaction: TransactionIdentifier;
    itemId: string;
    transactionDetailsAction: IActionItem;
    itemDetailsAction: IActionItem;
}

export interface UILabel {
    icon: string;
    text: string;
}

export interface TransactionIdentifier {
    sequenceNumber: number;
    deviceId: string;
    businessDate: string;
    voidedSequenceNumber: number;
}
