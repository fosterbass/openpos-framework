import { IActionItem } from '../../../../core/actions/action-item.interface';
import { SwatchInterface } from './swatch.interface';

export interface SwatchProductOptionPartInterface {
    optionId: string;
    swatches: SwatchInterface[];
    optionName?: string;
    selectedOption?: string;
    selectOptionAction: IActionItem;
    optionPlaceholder: string;
}
