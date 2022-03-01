import { FormControl } from '@angular/forms';
import { CustomDateValidator } from './custom-date-validators';

describe('MinDateValidator Test', () => {
    const minDateValidator = CustomDateValidator.minDate(new Date('06/19/2016').getTime());
    const maxDateValidator = CustomDateValidator.maxDate(new Date('06/19/2016').getTime());
    const minDateValidatorDate = CustomDateValidator.minDate(new Date('06/19/2016'));
    const maxDateValidatorDate = CustomDateValidator.maxDate(new Date('06/19/2016'));
    const minDateValidatorString = CustomDateValidator.minDate('06/19/2016');
    const maxDateValidatorString = CustomDateValidator.maxDate('06/19/2016');
    let formControl: FormControl;

    beforeEach(() => {
        formControl = new FormControl();
    });

    describe('minDate', () => {
        it('returns null when the field is empty', () => {
            const errors = minDateValidator(formControl);
            expect(errors).toBeNull();
            const errorsStringValidate = minDateValidatorString(formControl);
            expect(errorsStringValidate).toBeNull();
            const errorsDateValidate = minDateValidatorDate(formControl);
            expect(errorsDateValidate).toBeNull();
        });
        it('returns null when date is invalid', () => {
            formControl.setValue('not a date');
            const errors = minDateValidator(formControl);
            expect(errors).toBeNull();
            const errorsStringValidate = minDateValidatorString(formControl);
            expect(errorsStringValidate).toBeNull();
            const errorsDateValidate = minDateValidatorDate(formControl);
            expect(errorsDateValidate).toBeNull();
        });
        it('returns null when date is more recent than the minimum', () => {
            formControl.setValue('06/20/2016');
            const errors = minDateValidator(formControl);
            expect(errors).toBeNull();
            const errorsStringValidate = minDateValidatorString(formControl);
            expect(errorsStringValidate).toBeNull();
            const errorsDateValidate = minDateValidatorDate(formControl);
            expect(errorsDateValidate).toBeNull();
        });
        it('returns an error when date is older than the minimum', () => {
            formControl.setValue('06/18/2016');
            const errors = minDateValidator(formControl);
            expect(errors).toEqual({ minDate: { valid: false } });
            const errorsStringValidate = minDateValidatorString(formControl);
            expect(errorsStringValidate).toEqual({ minDate: { valid: false } });
            const errorsDateValidate = minDateValidatorDate(formControl);
            expect(errorsDateValidate).toEqual({ minDate: { valid: false } });
        });
    });
    describe('maxDate', () => {
        it('returns null when the field is empty', () => {
            const errors = maxDateValidator(formControl);
            expect(errors).toBeNull();
            const errorsStringValidate = maxDateValidatorString(formControl);
            expect(errorsStringValidate).toBeNull();
            const errorsDateValidate = maxDateValidatorDate(formControl);
            expect(errorsDateValidate).toBeNull();
        });
        it('returns null when date is invalid', () => {
            formControl.setValue('not a date');
            const errors = maxDateValidator(formControl);
            expect(errors).toBeNull();
            const errorsStringValidate = maxDateValidatorString(formControl);
            expect(errorsStringValidate).toBeNull();
            const errorsDateValidate = maxDateValidatorDate(formControl);
            expect(errorsDateValidate).toBeNull();
        });
        it('returns null when date is older than the maximum', () => {
            formControl.setValue('06/18/2016');
            const errors = maxDateValidator(formControl);
            expect(errors).toBeNull();
            const errorsStringValidate = maxDateValidatorString(formControl);
            expect(errorsStringValidate).toBeNull();
            const errorsDateValidate = maxDateValidatorDate(formControl);
            expect(errorsDateValidate).toBeNull();
        });
        it('returns an error when date is more recent than the maximum', () => {
            formControl.setValue('06/20/2016');
            const errors = maxDateValidator(formControl);
            expect(errors).toEqual({ maxDate: { valid: false } });
            const errorsStringValidate = maxDateValidatorString(formControl);
            expect(errorsStringValidate).toEqual({ maxDate: { valid: false } });
            const errorsDateValidate = maxDateValidatorDate(formControl);
            expect(errorsDateValidate).toEqual({ maxDate: { valid: false } });
        });
    });
});
