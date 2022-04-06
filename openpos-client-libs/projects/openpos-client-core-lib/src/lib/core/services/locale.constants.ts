export type LocaleConstantKey = 'currencySymbol' | 'currencyCode' | 'displayName' | 'localeIcon';
export const LOCALE_CONSTANTS = {
    currencySymbol: {
        'en-us': '$',
        'en-ca': '$',
        'en-gb': '£',
        'en-eu': '€',
        'fr-us': '$',
        'fr-ca': '$',
        default: '$'
    },
    currencyCode: {
        'en-us': 'USD',
        'fr-us': 'USD',
        'en-ca': 'CAD',
        'en-gb': 'GBP',
        'en-eu': 'EUR',
        'fr-ca': 'CAD',
        default: 'USD'
    },
    displayName: {
        'en-us': 'English',
        'fr-us': 'French',
        'en-ca': 'English',
        'en-gb': 'English',
        'en-eu': 'English',
        'fr-ca': 'French',
        default: 'English'
    },
    localeIcon: {
        'en-us': 'UnitedStates',
        'fr-us': 'France',
        'en-ca': 'Canada',
        'en-gb': 'GreatBritain',
        'en-eu': 'GreatBritain',
        'fr-ca': 'France',
        default: 'UnitedStates'
    }
};
