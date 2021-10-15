import { FormControl, ValidatorFn } from '@angular/forms';

export class CustomDateValidator {
    static minDate(minTime: number): ValidatorFn {
        return (field: FormControl) => {
            if (field.value) {
                const inputDate = new Date(field.value);
                if (!isNaN(inputDate.getTime())) {
                    const minDate = new Date(minTime);
                    if (inputDate < minDate) {
                        return { minDate: { valid: false } };
                    }
                }
            }
            return null;
        };
    }
    static maxDate(maxTime: number): ValidatorFn {
        return (field: FormControl) => {
            if (field.value) {
                const inputDate = new Date(field.value);
                if (!isNaN(inputDate.getTime())) {
                    const maxDate = new Date(maxTime);
                    if (inputDate > maxDate) {
                        return { maxDate: { valid: false } };
                    }
                }
            }
            return null;
        };
    }
}
