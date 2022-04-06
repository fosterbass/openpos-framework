import { Injectable } from '@angular/core';
import { LocaleService } from './locale.service';
import { IFormatter } from '../../shared/formatters/formatter.interface';
import { PhoneUSFormatter } from '../../shared/formatters/phone-us.formatter';
import { PhoneCAFormatter } from '../../shared/formatters/phone-ca.formatter';
import { DateTimeCAFormatter } from '../../shared/formatters/datetime-ca.formatter';
import { PostalCodeCAFormatter } from '../../shared/formatters/postal-code-ca.formatter';
import { NumericFormatter } from '../../shared/formatters/numeric.formatter';
import { GiftCodeFormatter } from '../../shared/formatters/gift-code.formatter';
import { MoneyFormatter } from '../../shared/formatters/money.formatter';
import { PercentageFormatter } from '../../shared/formatters/percentage.formatter';
import { PostalCodeFormatter } from '../../shared/formatters/postal-code.formatter';
import { IncomeFormatter } from '../../shared/formatters/income.formatter';
import { StateIDNumberFormatter } from '../../shared/formatters/state-id-number.formatter';
import { DecimalFormatter } from '../../shared/formatters/decimal.formatter';
import { WordTextFormatter } from '../../shared/formatters/word-text.formatter';
import { DateTimeFormatter } from '../../shared/formatters/datetime.formatter';
import { TimeFormat, TimeFormatter } from '../../shared/formatters/time.formatter';
import { DoNothingFormatter } from '../../shared/formatters/do-nothing.formatter';
import { NonNumericFormatter } from '../../shared/formatters/non-numeric.formatter';
import { WeightFormatter } from '../../shared/formatters/weight.formatter';
import { PostalCodeGenericFormatter } from '../../shared/formatters/postal-code-generic.formatter';


@Injectable({
    providedIn: 'root',
})
export class FormattersService {
    private formatters = new Map<string, Map<string, IFormatter>>();

    constructor(private localeService: LocaleService) {
        const usFormatters = new Map<string, IFormatter>();
        const defaultPhoneFormatter = new PhoneUSFormatter();

        usFormatters.set('phone', defaultPhoneFormatter);
        usFormatters.set('postalcodegeneric', new PostalCodeGenericFormatter());
        this.formatters.set('en-us', usFormatters);
        this.formatters.set('us', usFormatters);

        const caFormatters = new Map<string, IFormatter>();
        caFormatters.set('phone', new PhoneCAFormatter());
        caFormatters.set('datetime', new DateTimeCAFormatter());
        caFormatters.set('postalcode', new PostalCodeCAFormatter());
        caFormatters.set('postalcodegeneric', new PostalCodeGenericFormatter());

        // Some screens are dependent on 'ca' value, so don't change.  If you have other
        // ca formatters that are language specific, add a second entry in the map for them.
        this.formatters.set('ca', caFormatters);
        this.formatters.set('en-ca', caFormatters);

        const ukFormatters = new Map<string, IFormatter>();
        ukFormatters.set('datetime', new DateTimeCAFormatter());
        this.formatters.set('gb', ukFormatters);
        this.formatters.set('en-gb', ukFormatters);

        // If there isn't a specific formatter for a given locale, we fall back these
        const noLocaleFormatters = new Map<string, IFormatter>();
        this.formatters.set('NO-LOCALE', noLocaleFormatters);
        // Default formatters if no locale specific
        const numericFormatter = new NumericFormatter();
        noLocaleFormatters.set('numeric', numericFormatter);
        noLocaleFormatters.set('nonnumerictext', new NonNumericFormatter());
        noLocaleFormatters.set('numerictext', numericFormatter);
        noLocaleFormatters.set('giftcode', new GiftCodeFormatter());
        // Use USD formatter as default
        noLocaleFormatters.set('money', new MoneyFormatter(localeService.getLocale(), localeService.getConstant('currencyCode')));
        noLocaleFormatters.set('moneyEu', new MoneyFormatter('en_EU', 'EUR'));
        noLocaleFormatters.set('phone', numericFormatter);
        noLocaleFormatters.set('percent', new PercentageFormatter());
        noLocaleFormatters.set('percentint', new PercentageFormatter(PercentageFormatter.INTEGER_MODE));
        noLocaleFormatters.set('uspostalcode', numericFormatter);
        noLocaleFormatters.set('income', new IncomeFormatter());
        noLocaleFormatters.set('stateidnumber', new StateIDNumberFormatter());
        noLocaleFormatters.set('decimal', new DecimalFormatter());
        noLocaleFormatters.set('weight', new WeightFormatter());
        noLocaleFormatters.set('wordtext', new WordTextFormatter());
        noLocaleFormatters.set('datetime', new DateTimeFormatter());
        noLocaleFormatters.set('hour', new TimeFormatter(TimeFormat.HOUR));
        noLocaleFormatters.set('minsec', new TimeFormatter(TimeFormat.MIN_SEC));
        noLocaleFormatters.set('monthdate', new DateTimeFormatter('MM/dd'));
        noLocaleFormatters.set('monthdateyear', new DateTimeFormatter('MM/dd/yyyy'));
    }

    getFormatter(name: string): IFormatter {

        const locale = this.localeService.getLocale();
        if (name && locale) {
            const lname = name.toLowerCase();
            const llocale = locale.toLowerCase();
            // see if we have a validator map for the current locale
            //  and that locale has the validator we need
            if (this.formatters.get(llocale) && this.formatters.get(llocale).get(lname)) {
                return this.formatters.get(llocale).get(lname);
            }

            if (this.formatters.get('NO-LOCALE') && this.formatters.get('NO-LOCALE').get(lname)) {
                return this.formatters.get('NO-LOCALE').get(lname);
            }
        }

        if (name) {
            console.debug(`No formatter found for locale '${locale}' formatter name '${name}'. Using a 'Do Nothing' formatter`);
        }
        return new DoNothingFormatter();
    }

    setFormatter(name: string, formatter: IFormatter, locale?: string) {
        if (!locale) {
            locale = 'NO-LOCALE';
        }

        this.formatters.get(locale).set(name, formatter);
    }
}
