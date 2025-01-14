import { IValidator, IValidatorSpec } from '../../core/interfaces/validator.interface';
import { ValidatorFn, FormControl } from '@angular/forms';

export class MinValueValidator implements IValidator {
    name = this.constructor.name;

    constructor(public spec: MinValueValidatorSpec) {
    }

    validationFunc: ValidatorFn = (ctrl: FormControl) => {
        let value = ctrl.value;
        if (value) {
            value = value.replace(',', '');
        }
        return Number(value) >= Number(this.spec.minimumValue) ? null : {
            minvalue: {
                valid: false
            }
        };
    }
}

export interface MinValueValidatorSpec extends IValidatorSpec {
    name: string;
    minimumValue: string;
}
