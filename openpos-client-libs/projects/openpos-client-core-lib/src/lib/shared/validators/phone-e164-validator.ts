import { IValidator } from '../../core/interfaces/validator.interface';
import { ValidatorFn, FormControl } from '@angular/forms';

export class PhoneE164Validator implements IValidator {
    name = 'PhoneE164';

    validationFunc: ValidatorFn = PhoneE164ValidatorFn;
}

export function PhoneE164ValidatorFn(ctrl: FormControl) {
    const regex = /^[+]?[1-9][0-9]{10,14}$/;
    if (ctrl.value) {
        return regex.test(ctrl.value) ? null : {
            phoneE164: {
                valid: false
            }
        };
    } else {
        return null;
    }
}
