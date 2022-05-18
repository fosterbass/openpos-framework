import { IDataTableRow } from '../../../shared/components/grid-table/data-table-row.interface';


export interface IProductPromotions {
    promotionsTitle: string;
    icon: string;
    noPromotionsLabel: string;
    promotionStackingDisclaimer: string;
    promotionDetails: IDataTableRow[];
}
