import { FormControl, ValidatorFn } from '@angular/forms';

export class CustomDateValidator {
    private constructor() { }

    static minDate(minTime: Date | number): ValidatorFn {
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

    static maxDate(maxTime: Date | number): ValidatorFn {
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

    private static _resolveTime(time: Date | number): Date {
        if (typeof time === 'number') {
            return new Date(time);
        } else {
            return time;
        }
    }
}
