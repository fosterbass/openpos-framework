import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { ILocationProvider } from './location-provider.interface';
import { ILocationData } from './location-data.interface';
import { CONFIGURATION } from '../../configuration/configuration';

interface GoogleGeocodeResponse {
    results: GoogleGeocodeResult[];
}

interface GoogleGeocodeResult {
    address_components: GoogleAddressComponent[];
}

interface GoogleAddressComponent {
    long_name: string;
    short_name: string;
    types: string[];
}

@Injectable({
    providedIn: 'root'
})
export class LocationProviderDefault implements ILocationProvider {

    prevLat: number;
    prevLong: number;

    private $locationData = new BehaviorSubject<ILocationData>(null);

    constructor(private http: HttpClient) {
    }

    getProviderName(): string {
        return 'default';
    }

    getCurrentLocation(coordinateBuffer: number): Observable<ILocationData> {
        if (navigator.geolocation && CONFIGURATION.googleApiKey) {
            const previous = { latitude: 0, longitude: 0 };
            const buffer = coordinateBuffer;
            navigator.geolocation.watchPosition((position) => {
                const lat = position.coords.latitude;
                const long = position.coords.longitude;
                if (lat > previous.latitude + buffer || lat < previous.latitude - buffer
                    || long > previous.longitude + buffer || long < previous.longitude - buffer) {
                    previous.latitude = lat;
                    previous.longitude = long;
                    const latlong = `${lat},${long}`;
                    console.log('calling google maps geocode api');
                    this.reverseGeocode(CONFIGURATION.googleApiKey, latlong)
                        .then((response: GoogleGeocodeResponse) => this.extractZipAndCountry(response))
                        .catch((error) => console.log(error));
                }
            });
        }
        return this.$locationData;
    }

    reverseGeocode(key: string, param: string): Promise<GoogleGeocodeResponse> {
        try {
            return this.http
                .get<GoogleGeocodeResponse>('https://maps.google.com/maps/api/geocode/json?key=' + key + '&latlng=' + param + '&sensor=false')
                .toPromise();
        } catch (error) {
            return Promise.resolve(error.json());
        }
    }

    private extractZipAndCountry(googleResponse: GoogleGeocodeResponse): void {
        let zipCode = '';
        let countryName = '';
        console.log(googleResponse.results[0].address_components);
        for (const addressComponent of googleResponse.results[0].address_components) {
            for (const type of addressComponent.types) {
                if (type === 'postal_code') {
                    zipCode = addressComponent.long_name;
                }
                if (type === 'country') {
                    countryName = addressComponent.long_name;
                }
            }
        }

        this.$locationData.next({
            type: 'default',
            postalCode: zipCode,
            country: countryName
        } as ILocationData);
    }
}
