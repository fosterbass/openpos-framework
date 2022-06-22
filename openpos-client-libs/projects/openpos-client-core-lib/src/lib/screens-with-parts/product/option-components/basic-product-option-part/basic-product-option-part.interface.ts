import { IActionItem } from '../../../../core/actions/action-item.interface';
import { BasicOptionInterface } from './basic-option.interface';

export interface BasicProductOptionPartInterface {
    optionId: string;
    optionName: string;
    optionPlaceholder: string;
    options: BasicOptionInterface[];
    selectOptionAction: IActionItem;
    selectedOption: string;
}
