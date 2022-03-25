import { VALIDATION_CONSTANTS } from './../../shared/validators/validation.constants';
import { IValidator, IValidatorSpec } from './../interfaces/validator.interface';
import { Injectable } from '@angular/core';
import { ValidatorFn, Validators } from '@angular/forms';
// Since there are directives in shared that import validator service using the ../../shared
// barrel here causes a circular reference
import { LocaleService } from './locale.service';
import { OpenPosValidators } from '../../shared/validators/openpos-validators';

@Injectable({
    providedIn: 'root',
})
export class ValidatorsService {

    private validators = new Map<string, Map<string, IValidator>>();

    constructor(private localeService: LocaleService) {
        const US_VALIDATORS = new Map<string, IValidator>();
        const NO_LOCALE_VALIDATORS = new Map<string, IValidator>();
        const CA_VALIDATORS = new Map<string, IValidator>();
        const HKG_VALIDATORS = new Map<string, IValidator>();

        US_VALIDATORS.set('phone', OpenPosValidators.PHONE_US);
        CA_VALIDATORS.set('phone', OpenPosValidators.PHONE_CA);
        CA_VALIDATORS.set('postalcode', { name: 'PostalCode', validationFunc: Validators.minLength(6) });

        HKG_VALIDATORS.set('phone', OpenPosValidators.PHONE_HKG);

        NO_LOCALE_VALIDATORS.set('giftcode', OpenPosValidators.GIFT_CODE);
        NO_LOCALE_VALIDATORS.set('date', OpenPosValidators.DATE_MMDDYYYY);
        NO_LOCALE_VALIDATORS.set('datemmddyy', OpenPosValidators.DATE_MMDDYY);
        NO_LOCALE_VALIDATORS.set('dateddmmyyyy', OpenPosValidators.DATE_DDMMYYYY);
        NO_LOCALE_VALIDATORS.set('dateddmmyy', OpenPosValidators.DATE_DDMMYY);

        NO_LOCALE_VALIDATORS.set('email', { name: 'Email', validationFunc: Validators.email });
        NO_LOCALE_VALIDATORS.set('postalcode', { name: 'PostalCode', validationFunc: Validators.minLength(5) });
        NO_LOCALE_VALIDATORS.set('gt_0', OpenPosValidators.GT_0);
        NO_LOCALE_VALIDATORS.set('gte_0', OpenPosValidators.GTE_0);

        this.validators.set('us', US_VALIDATORS);
        this.validators.set('ca', CA_VALIDATORS);
        this.validators.set('hkg', HKG_VALIDATORS);

        this.validators.set('NO-LOCALE', NO_LOCALE_VALIDATORS);
    }

    getValidator(validatorSpec: string | IValidatorSpec): ValidatorFn {
        const region = this.localeService.getRegion();
        if (typeof validatorSpec === 'string') {
            if (validatorSpec && region) {
                const lname = validatorSpec.toLowerCase();
                const lregion = region.toLowerCase();
                // see if we have a validator map for the current region
                //  and that region has the validator we need
                if (this.validators.get(lregion) && this.validators.get(lregion).get(lname)) {
                    const v = this.validators.get(lregion).get(lname);
                    return typeof v !== 'undefined' ? v.validationFunc : undefined;
                }

                if (this.validators.get('NO-LOCALE') && this.validators.get('NO-LOCALE').get(lname)) {
                    const v = this.validators.get('NO-LOCALE').get(lname);
                    return typeof v !== 'undefined' ? v.validationFunc : undefined;
                }
            }
            console.info(`No validator found for locale '${region}' validator name '${validatorSpec}'. Using an 'always valid' validator`);
            return () => null;
        } else {
            // Support for dynamically loading validators specified by the server side.
            // If region support is needed, that can be handled in the validator implementation
            const validatorInstance: IValidator =
                VALIDATION_CONSTANTS.validators.find(entry => entry.name === validatorSpec.name).validatorClass.prototype;
            validatorInstance.constructor.apply(validatorInstance, [validatorSpec]);
            return validatorInstance.validationFunc;
        }
    }

    public overrideValidator(locale: string, validatorName: string, validator: IValidator) {
        const map = this.validators.get(locale);
        if (map) {
            map.set(validatorName, validator);
        }
    }
}
