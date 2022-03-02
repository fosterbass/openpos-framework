import { FormControl, ValidatorFn } from '@angular/forms';

type ValidDateFormat = Date | number | string;
export class CustomDateValidator {
    private constructor() { }


    static minDate(minTime: ValidDateFormat): ValidatorFn {
        return (field: FormControl) => {
            if (field.value) {
                const inputDate = new Date(field.value);
                if (!isNaN(inputDate.getTime())) {
                    const minDate = this._resolveTime(minTime);
                    if (inputDate < minDate) {
                        return { minDate: { valid: false } };
                    }
                }
            }
            return null;
        };
    }

    static maxDate(maxTime: ValidDateFormat): ValidatorFn {
        return (field: FormControl) => {
            if (field.value) {
                const inputDate = new Date(field.value);
                if (!isNaN(inputDate.getTime())) {
                    const maxDate = this._resolveTime(maxTime);
                    if (inputDate > maxDate) {
                        return { maxDate: { valid: false } };
                    }
                }
            }
            return null;
        };
    }

    private static _resolveTime(time: ValidDateFormat): Date {
        if (typeof time === 'number' || typeof time === 'string') {
            return new Date(time);
        } else {
            return time;
        }
    }
}
