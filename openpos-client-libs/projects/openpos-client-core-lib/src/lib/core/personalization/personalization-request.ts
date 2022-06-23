
export interface PersonalizationRequest {
    deviceToken?: string;
    deviceId?: string;
    appId?: string;
    pairedAppId?: string;
    pairedDeviceId?: string;
    businessUnitId?: string;
    personalizationParameters?: { [key: string]: string };
}

export interface PersonalizationResponse {
    authToken: string;
    deviceModel: IDeviceModel;
}

export interface IDeviceModel {
    deviceId: string;
    appId: string;
    deviceParamModels: IDeviceParamModel[];
}

export interface IDeviceParamModel {
    paramName: string;
    paramValue: string;
}
