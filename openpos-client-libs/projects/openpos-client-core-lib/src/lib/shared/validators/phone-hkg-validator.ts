import { IValidator } from '../../core/interfaces/validator.interface';
import { ValidatorFn, FormControl } from '@angular/forms';

export class PhoneHKGValidator implements IValidator {
    name = 'PhoneHKG';

    validationFunc: ValidatorFn = PhoneHKGValidatorFn;
}

export function PhoneHKGValidatorFn(ctrl: FormControl) {
    const regex = /^[0-9]{11}$/;
    if (ctrl.value) {
        return regex.test(ctrl.value) ? null : {
            phoneHKG: {
                valid: false
            }
        };
    } else {
        return null;
    }
}
