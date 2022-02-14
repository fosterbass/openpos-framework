import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Subject, of } from 'rxjs';
import { ILocationProvider, LocationProviderConfigChanged } from './location-provider.interface';
import { ILocationData } from './location-data.interface';
import { CONFIGURATION } from '../../configuration/configuration';
import { ConfigurationService } from '../services/configuration.service';
import { catchError, filter, take, timeout } from 'rxjs/operators';

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

interface LocationAddressInfo {
    postalCode: string;
    country: string;
}

@Injectable({
    providedIn: 'root'
})
export class LocationProviderDefault implements ILocationProvider {
    static readonly MAX_CURRENT_POS_ATTEMPTS = 2;

    coordinateBuffer: number;
    private $locationData = new BehaviorSubject<ILocationData>(null);

    constructor(private http: HttpClient, private configurationService: ConfigurationService) {
        this.configurationService.getConfiguration('uiConfig')
            .pipe(filter(config => Object.keys(config).includes('googleApiKey')), take(1))
            .subscribe((config: LocationProviderConfigChanged) => {
                if (this.coordinateBuffer != null) {
                    this.getCurrentLocation(this.coordinateBuffer, config.googleApiKey);
                }
            });
    }

    getProviderName(): string {
        return 'default';
    }

    getCurrentLocation(buffer: number, googleApiKey?: string): Observable<ILocationData> {
        this.coordinateBuffer = buffer;
        console.info(`[LocationProviderDefault] navigator.geolocation: ${navigator.geolocation}`);
        if (navigator.geolocation && (CONFIGURATION.googleApiKey || googleApiKey)) {
            console.info(`[LocationProviderDefault] Got GoogleAPI Key. Attempting to get current GPS position...`);
            const curPosition$ = new Subject<{latitude: number, longitude: number}>();
            // Getting GPS coords can take a while, thus the long timeout
            this.getCurrentPosition(CONFIGURATION.googleApiKey, {timeout: 20000, enableHighAccuracy: true}, 0).
                then(async (initPosition) => {
                    console.info(`[LocationProviderDefault] Initial position: ${initPosition}`);
                    if (initPosition != null) {
                        const initPos = {latitude: initPosition.coords.latitude, longitude: initPosition.coords.longitude};
                        await this.invokeGoogleMapsApiAndEmitLocation(CONFIGURATION.googleApiKey, initPos.latitude, initPos.longitude);
                        curPosition$.next(initPos);
                    } else {
                        console.warn(`[LocationProviderDefault] All methods to get an initial position failed.`);
                        curPosition$.next({latitude: 0, longitude: 0});
                    }
                },
                err => {
                    console.warn(`[LocationProviderDefault] Error getting initial position: ${JSON.stringify(err)}`);
                    curPosition$.next({latitude: 0, longitude: 0});
                });

            curPosition$.subscribe( previousPos => {
                console.info(`[LocationProviderDefault] Now starting geolocation.watchPosition with previousPos: ${JSON.stringify(previousPos)}`);
                navigator.geolocation.watchPosition((position) => {
                    const lat = position.coords.latitude;
                    const long = position.coords.longitude;
                    if (lat > previousPos.latitude + this.coordinateBuffer || lat < previousPos.latitude - this.coordinateBuffer
                        || long > previousPos.longitude + this.coordinateBuffer || long < previousPos.longitude - this.coordinateBuffer) {
                        previousPos.latitude = lat;
                        previousPos.longitude = long;

                        this.invokeGoogleMapsApiAndEmitLocation(CONFIGURATION.googleApiKey, lat, long);
                    }
                });
            });

        }
        return this.$locationData;

    }

    private getCurrentPosition(googleApiKey: string, options: PositionOptions, attemptCount: number): Promise<Position> {
        return new Promise((resolve, reject) => {
            attemptCount++;
            navigator.geolocation.getCurrentPosition(
                resolve,
                (err) => {
                    if (attemptCount < LocationProviderDefault.MAX_CURRENT_POS_ATTEMPTS) {
                        console.warn(`[LocationProviderDefault] Attempt ${attemptCount} of ${LocationProviderDefault.MAX_CURRENT_POS_ATTEMPTS} ` +
                            `to getCurrentPosition failed, trying again. Error code: ${err.code}, ${err.message}`);
                        resolve(this.getCurrentPosition(googleApiKey, options, attemptCount));
                    } else {
                        console.warn(`[LocationProviderDefault] Last getCurrentPosition attempt failed. Error code: ${err.code}, ${err.message}`);
                        resolve(null);
                        // Google API doesn't return a very accurate location, particularly if you are connected through a VPN
                        // resolve(this.getGoogleAPILocation(googleApiKey));
                    }
                },
                options
            ); }
        );
    }

    private async invokeGoogleMapsApiAndEmitLocation(apiKey: string, lat: number, long: number) {
        const latlong = lat + ',' + long;
        let locationAddressInfo: LocationAddressInfo;

        await this.reverseGeocode(apiKey, latlong)
            .then((response) => {
                console.log(`[LocationProviderDefault] Geocode response: ${response?.results?.length > 0 ? JSON.stringify(response.results[0].address_components) : 'empty'}`);
                locationAddressInfo = this.extractZipAndCountry(response);

                const data = {
                    type: 'default',
                    postalCode: locationAddressInfo.postalCode,
                    latitude: lat.toString(),
                    longitude: long.toString(),
                    country: locationAddressInfo.country
                } as ILocationData;
                console.info(`[LocationProviderDefault] new locationData: ${JSON.stringify(data)}`);
                this.$locationData.next(data);
            })
            .catch((error) => console.log(error));
    }

    async reverseGeocode(key: string, param: string): Promise<GoogleGeocodeResponse> {
        try {
            console.log(`[LocationProviderDefault] Calling google maps geocode api with lat,long: ${param}`);
            return this.http
                .get<GoogleGeocodeResponse>(`https://maps.google.com/maps/api/geocode/json?key=${key}&latlng=${param}&sensor=false`)
                .toPromise();
        } catch (error) {
            return Promise.resolve(error.json());
        }
    }

    private extractZipAndCountry(googleResponse: GoogleGeocodeResponse): LocationAddressInfo {
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

        return {postalCode: zipCode, country: countryName};
    }

    /**
     * Alternate way to get location via google geolocation API.  Uses network info for location
     * so it is influenced by whatever networks are involved with the connection.  Not accurate at
     * all when connected over VPN.
     */
    private async getGoogleAPILocation(googleApiKey: string): Promise<Position> {
        console.log(`[LocationProviderDefault] Calling google location api to get location`);
        const response = await this.http
            .post(`https://www.googleapis.com/geolocation/v1/geolocate?key=${googleApiKey}`, {})
            .pipe(
                timeout(3000),
                catchError(e => {
                    console.warn(`[LocationProviderDefault] Google location api failed with error: ${JSON.stringify(e)}`);
                    return of(null);
                })
            ).toPromise();
        if (response != null) {
            const result = response.json();
            console.info(`[LocationProviderDefault] Google location api returned: ${JSON.stringify(result)}`);
            if (result.location) {
                return {
                    coords: {
                        latitude: result.location.lat,
                        longitude: result.location.lng,
                        accuracy: result.accuracy,
                        altitude: null,
                        altitudeAccuracy: null,
                        heading: null,
                        speed: null
                    },
                    timestamp: Date.now()
                };
            } else {
                console.warn(`[LocationProviderDefault] No location found on Google location api result: ${JSON.stringify(result)}`);
                return null;
            }
        } else {
            console.warn(`[LocationProviderDefault] null response from Google location api`);
            return null;
        }
    }

}
