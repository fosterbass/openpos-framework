import { ServerLocation } from './server-location';

export interface AutoPersonalizationParametersResponse {
    deviceName: string;
    serverAddress: string;
    serverPort: string;
    deviceId: string;
    appId: string;
    personalizationParams?: any;
    sslEnabled?: boolean;
    failovers?: ServerLocation[];
}
