import { ServerLocation } from './server-location';

export interface AutoPersonalizationParametersResponse {
    deviceToken: string;
    deviceName: string;
    serverAddress: string;
    serverPort: string;
    deviceId: string;
    appId: string;
    personalizationParams?: any;
    sslEnabled?: boolean;
    failoverAddresses?: ServerLocation[];
}
