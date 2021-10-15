import { FormControl, ValidatorFn } from '@angular/forms';
import { CustomDateValidator } from './custom-date-validators';

describe('MinDateValidator Test', () => {
    const minDateValidator = CustomDateValidator.minDate(new Date('06/19/2016').getTime());
    const maxDateValidator = CustomDateValidator.maxDate(new Date('06/19/2016').getTime());
    let formControl: FormControl;

    beforeEach(() => {
        formControl = new FormControl();
    });

    describe('minDate', () => {
        it('returns null when the field is empty', () => {
            const errors = minDateValidator(formControl);
            expect(errors).toBeNull();
        });
        it('returns null when date is invalid', () => {
            formControl.setValue('not a date');
            const errors = minDateValidator(formControl);
            expect(errors).toBeNull();
        });
        it('returns null when date is more recent than the minimum', () => {
            formControl.setValue('06/20/2016');
            const errors = minDateValidator(formControl);
            expect(errors).toBeNull();
        });
        it('returns an error when date is older than the minimum', () => {
            formControl.setValue('06/18/2016');
            const errors = minDateValidator(formControl);
            expect(errors).toEqual({ minDate: { valid: false } });
        });
    });
    describe('maxDate', () => {
        it('returns null when the field is empty', () => {
            const errors = maxDateValidator(formControl);
            expect(errors).toBeNull();
        });
        it('returns null when date is invalid', () => {
            formControl.setValue('not a date');
            const errors = maxDateValidator(formControl);
            expect(errors).toBeNull();
        });
        it('returns null when date is older than the maximum', () => {
            formControl.setValue('06/18/2016');
            const errors = maxDateValidator(formControl);
            expect(errors).toBeNull();
        });
        it('returns an error when date is more recent than the maximum', () => {
            formControl.setValue('06/20/2016');
            const errors = maxDateValidator(formControl);
            expect(errors).toEqual({ maxDate: { valid: false } });
        });
    });
});
