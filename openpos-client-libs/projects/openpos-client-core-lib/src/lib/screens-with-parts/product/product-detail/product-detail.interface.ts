import { IActionItem } from '../../../core/actions/action-item.interface';
import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { DisplayProperty } from '../../../shared/components/display-property/display-property.interface';
import { ITabContent } from '../../../shared/components/tabbed-content-card/tab-content.interface';
import { ITab } from '../../../shared/components/tabbed-content-card/tab.interface';
import { ProductOptionInterface } from '../product-option.interface';
import { AvailabilityType } from './availability-type.enum';
import { IProductInventory } from './product-inventory.interface';
import { IProductPromotions } from './product-promotions.interface';

export interface ProductDetailInterface extends IAbstractScreen {
    productName: string;
    productIdentifiers: DisplayProperty[];
    price: string;
    availabilityLabel: string;
    availabilityType: AvailabilityType;
    orderAvailabilityLabel: string;
    imageUrls: string[];
    alternateImageUrl: string;
    productOptionsComponents: ProductOptionInterface[];
    tabs: ITab[];
    tabContents: ITabContent[];
    productPromotions: IProductPromotions;
    productInventory: IProductInventory;
    actions: IActionItem[];
}
