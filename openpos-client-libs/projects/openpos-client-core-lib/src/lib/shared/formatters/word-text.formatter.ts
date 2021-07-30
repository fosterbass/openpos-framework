import { IFormatter } from './formatter.interface';

export class WordTextFormatter implements IFormatter {
    static readonly CHAR_REGEX = /[^a-zA-Z0-9]/g;
    static readonly FILTER_REGEX = /^[a-zA-Z0-9]$/;

    allowKey(key: string, newValue: string): boolean {
        return WordTextFormatter.FILTER_REGEX.test(key);
    }

    formatValue(value: string): string {
        if (value == undefined || value == null) {
            return null;
        } else {
            return value.toString().replace(WordTextFormatter.CHAR_REGEX, '');
        }
    }

    unFormatValue(value: string): string {
        return value;
    }
}
