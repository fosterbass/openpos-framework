export type LocaleConstantKey = 'currencySymbol' | 'currencyCode' | 'displayName' | 'localeIcon';
export const LOCALE_CONSTANTS = {
    currencySymbol: {
        'en-us': '$',
        'en-ca': '$',
        'fr-us': '$',
        'fr-ca': '$',
        'es-mx': '$',
        default: '$'
    },
    currencyCode: {
        'en-us': 'USD',
        'fr-us': 'USD',
        'en-ca': 'CAD',
        'fr-ca': 'CAD',
        'es-mx': 'MXN',
        default: 'USD'
    },
    displayName: {
        'en-us': 'English',
        'fr-us': 'French',
        'en-ca': 'English',
        'fr-ca': 'French',
        'es-mx': 'Spanish',
        default: 'English'
    },
    localeIcon: {
        'en-us': 'UnitedStates',
        'fr-us': 'France',
        'en-ca': 'Canada',
        'fr-ca': 'France',
        'es-mx': 'Mexico',
        default: 'UnitedStates'
    }
};
