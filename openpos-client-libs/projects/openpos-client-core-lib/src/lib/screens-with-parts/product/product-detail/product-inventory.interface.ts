import { IInventoryDetail } from '../inventory-detail.interface';

export interface IProductInventory {
    inventoryTitle: string;
    icon: string;
    businessUnitInventoryDetails: IInventoryDetail[];
    noBuddyStoresMessage: string;
    buddyStoreOfflineMessage: string;
    buddyStoreProviderKey: string;
}
