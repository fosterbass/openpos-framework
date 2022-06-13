

export interface PersonalizationConfig {
    availableBusinessUnits: BusinessUnit[];
    storeDevices: { [key: string]: BusinessUnitDevice[] };
    parameters: PersonalizationParameter[];
    loadedAppIds: string[];
}

export interface BusinessUnit {
    id: string;
    name: string;
    locationHint?: string;
}

export interface BusinessUnitDevice {
    businessUnitId: string;
    deviceId: string;
    appId: string;
    authToken?: string;
    personalizationParamValues: { [key: string]: string };
    connected?: boolean;
}

export interface PersonalizationParameter {
    property: string;
    label: string;
    defaultValue?: string;
}
