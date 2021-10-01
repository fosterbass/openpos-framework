import { IAbstractScreen } from '../../core/interfaces/abstract-screen.interface';
import { IActionItem } from '../../core/actions/action-item.interface';

export interface EnrollmentConfirmationDialogInterface extends IAbstractScreen {
    programCopy: string;
    benefactorName: string;
    continueAction: IActionItem;
    signUpAnother: IActionItem;
    message: string;
    checkIcon: string;

}
