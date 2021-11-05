import { IAbstractScreen } from '../../../core/interfaces/abstract-screen.interface';
import { Plan } from '../plan-interface';

export interface ProgramPlanSelectDialogInterface extends IAbstractScreen {
    programCopy: string;
    subscriptionPlans: Plan[];
}
