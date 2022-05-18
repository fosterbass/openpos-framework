export interface IProductInventory {
    inventoryTitle: string;
    icon: string;
    onHandLabel: string;
    onHandCount: number;
    reservedLabel: string;
    reservedCount: number;
    damagedLabel: string;
    damagedCount: number;
    noBuddyStoresMessage: string;
    buddyStoreOfflineMessage: string;
    inventoryMessageProviderKey: string;
    buddyStoreProviderKey: string;
}
