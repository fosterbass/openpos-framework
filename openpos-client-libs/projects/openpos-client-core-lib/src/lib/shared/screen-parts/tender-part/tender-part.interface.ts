import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { IForm } from '../../../core/interfaces/form.interface';
import { ITotal } from '../../../core/interfaces/total.interface';
import { OptionsListInterface } from '../options-list/options-list.interface';
import { ITender } from './tender.interface';
import { IActionItem } from '../../../core/actions/action-item.interface';

export interface TenderPartInterface extends IAbstractScreen {
    form: IForm;
    title: string;
    prompt: string;
    amountDue: ITotal;
    amounts: ITender[];
    imageUrl: string;
    optionsList: OptionsListInterface;
    totalOptionsShownBeforeOverflow: number;
    roundUpAvailable: boolean;
    roundUpButton: IActionItem;
}
