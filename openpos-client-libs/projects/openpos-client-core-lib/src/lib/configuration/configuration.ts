export const CONFIGURATION = {
    mimicScroll: false,
    useOnScreenKeyboard: false,
    useTouchListener: true,
    useSimulatedScanner: false,
    showRegisterStatus: false,
    clickableRegisterStatus: false,
    offlineOnlyRegisterStatus: false,
    keepAliveMillis: 30000,
    maxSignaturePoints: -1,
    maxResponseSizeBytes: 79500,
    enableAutocomplete: false,
    enableMenuClose: true,
    enableKeybinds: false,
    googleApiKey: '',
    loadingDialogDelay: 4000,
    confirmConnectionTimeoutMillis: 172800000,
    autoPersonalizationRequestTimeoutMillis: 50000,
    autoPersonalizationServicePath: null,
    // These properties are static on the client and not overriden by configuration.service.ts
    compatibilityVersion: 'v1',
    incompatibleVersionMessage: 'Application is not compatible with the server.',
    autoFocusFirstOptionsListOption: false
};
