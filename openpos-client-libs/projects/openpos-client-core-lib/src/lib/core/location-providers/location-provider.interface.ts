import { ILocationData } from './location-data.interface';
import { Observable } from 'rxjs';

export interface ILocationProvider {
    getProviderName(): string;
    getCurrentLocation(coordinateBuffer: number): Observable<ILocationData>;
}
