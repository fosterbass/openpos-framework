export type LocaleConstantKey = 'currencySymbol' | 'currencyCode' | 'displayName' | 'localeIcon';
export const LOCALE_CONSTANTS = {
    currencySymbol: {
        'en-us': '$',
        'en-ca': '$',
        'fr-us': '$',
        'fr-ca': '$',
        default: '$'
    },
    currencyCode: {
        'en-us': 'USD',
        'fr-us': 'USD',
        'en-ca': 'CAD',
        'fr-ca': 'CAD',
        default: 'USD'
    },
    displayName: {
        'en-us': 'English',
        'fr-us': 'French',
        'en-ca': 'English',
        'fr-ca': 'French',
        default: 'English'
    },
    localeIcon: {
        'en-us': 'UnitedStates',
        'fr-us': 'France',
        'en-ca': 'Canada',
        'fr-ca': 'France',
        default: 'UnitedStates'
    }
};
