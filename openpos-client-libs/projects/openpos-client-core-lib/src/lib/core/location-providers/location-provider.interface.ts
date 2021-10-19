import { ILocationData } from './location-data.interface';
import { Observable } from 'rxjs';
import { ConfigChangedMessage } from '../messages/config-changed-message';

export interface ILocationProvider {
    getProviderName(): string;
    getCurrentLocation(coordinateBuffer: number): Observable<ILocationData>;
}

export interface LocationProviderConfigChanged extends ConfigChangedMessage {
    googleApiKey: string;
}
