import { IFormatter } from './formatter.interface';

export class PhoneE164Formatter implements IFormatter {
    private newValueFilter = /^[+]?[1-9]?[0-9]{0,14}$/;
    constructor() {
    }

    formatValue(value: string): string {
        if (!value) {
            return '';
        }
        return value;
    }

    unFormatValue(value: string): string {
        const n = value.replace(/[^+0-9]/g, '');
        return n;
    }

    allowKey(key: string, newValue: string) {
        return this.newValueFilter.test(newValue);
    }
}
