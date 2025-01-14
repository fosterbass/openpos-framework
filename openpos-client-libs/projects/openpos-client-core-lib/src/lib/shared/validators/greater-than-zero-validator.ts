import { IValidator } from '../../core/interfaces/validator.interface';
import { ValidatorFn, FormControl } from '@angular/forms';

export class GreaterThanZeroValidator implements IValidator {
    name = 'GT_0';

    validationFunc: ValidatorFn = (ctrl: FormControl) => {
        let value = ctrl.value;
        if (value) {
            value = value.replace(',', '');
        }
        return Number(value) > 0 ? null : {
            gt_0: {
                valid: false
            }
        };
    }
}
