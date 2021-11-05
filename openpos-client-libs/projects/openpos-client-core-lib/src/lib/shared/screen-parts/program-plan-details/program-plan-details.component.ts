import { Component, Input} from '@angular/core';
import { Plan } from '../../../screens-with-parts/membership/plan-interface';

@Component({
    selector: 'app-program-plan-details',
    templateUrl: './program-plan-details.component.html',
    styleUrls: ['./program-plan-details.component.scss']
})
export class ProgramPlanDetailsComponent {

    @Input()
    programCopy: string;

    @Input()
    plans: Plan[];

}
