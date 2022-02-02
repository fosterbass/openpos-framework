import { ILocationProvider } from './location-provider.interface';
import { ILocationData } from './location-data.interface';
import { Observable, of, Subject } from 'rxjs';
import { Http } from '@angular/http';
import { Configuration } from '../../configuration/configuration';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import {ConfigurationService} from "../services/configuration.service";
import { catchError, filter, take, timeout } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class LocationProviderDefault implements ILocationProvider {
    static readonly MAX_CURRENT_POS_ATTEMPTS = 2;

    coordinateBuffer: number;
    private $locationData = new BehaviorSubject<ILocationData>(null);

    constructor(private http: Http, private configurationService: ConfigurationService) {
        this.configurationService.getConfiguration('uiConfig')
            .pipe(filter(config => Object.keys(config).includes('googleApiKey')), take(1))
            .subscribe(config => {
            if(this.coordinateBuffer != null) {
                this.getCurrentLocation(this.coordinateBuffer, config['googleApiKey']);
            }
        })
    }

    getProviderName(): string {
        return 'default';
    }

    getCurrentLocation(buffer: number, googleApiKey?: string): Observable<ILocationData> {
        this.coordinateBuffer = buffer;
        console.info(`[LocationProviderDefault] navigator.geolocation: ${navigator.geolocation}, CONFIGURATION.googleApiKey: ${Configuration.googleApiKey}, googleApiKey: ${googleApiKey}`);
        if (navigator.geolocation && (Configuration.googleApiKey || googleApiKey)) {
            console.info(`[LocationProviderDefault] Attempting to get current GPS position...`);
            const curPosition$ = new Subject<{latitude: number, longitude: number}>();
            // Getting GPS coords can take a while, thus the long timeout
            this.getCurrentPosition(Configuration.googleApiKey, {timeout: 20000, enableHighAccuracy: true}, 0).then(async (initPosition) => {
                console.info(`[LocationProviderDefault] Got initial position: ${initPosition}`);

                if (initPosition != null) {
                    const initPos = {latitude: initPosition.coords.latitude, longitude: initPosition.coords.longitude};
                    await this.invokeGoogleMapsApiAndEmitLocation(Configuration.googleApiKey, initPos.latitude, initPos.longitude);
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

                        this.invokeGoogleMapsApiAndEmitLocation(Configuration.googleApiKey, lat, long);
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
                        console.warn(`[LocationProviderDefault] Last getCurrentPosition attempt failed, falling back to calling google. Error code: ${err.code}, ${err.message}`);
                        resolve(this.getGoogleAPILocation(googleApiKey));
                    }
                },
                options
            )}
        );
    }

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

    private async invokeGoogleMapsApiAndEmitLocation(apiKey: string, lat: number, long: number) {
        const latlong = lat + ',' + long;
        let zipCode = '';
        let  countryName = '';

        await this.reverseGeocode(apiKey, latlong)
            .then((response) => {
                console.log(`[LocationProviderDefault] Geocode response: ${response.results[0].address_components}`);
                for (const addressComponent of response.results[0].address_components) {
                    for (const type of addressComponent.types) {
                        if (type === 'postal_code') {
                            zipCode = addressComponent.long_name;
                        }
                        if (type === 'country') {
                            countryName = addressComponent.long_name;
                        }
                    }
                }

                const data = {
                    type: 'default',
                    postalCode: zipCode,
                    latitude: lat.toString(),
                    longitude: long.toString(),
                    country: countryName
                } as ILocationData;
                console.info(`[LocationProviderDefault] new locationData: ${JSON.stringify(data)}`);
                this.$locationData.next(data);
            })
            .catch((error) => console.log(error));


    }

    async reverseGeocode(key: string, param: string): Promise<any> {
        try {
            console.log(`[LocationProviderDefault] Calling google maps geocode api with lat,long: ${param}`);
            const response = await this.http
                .get('https://maps.google.com/maps/api/geocode/json?key=' + key + '&latlng=' + param + '&sensor=false')
                .toPromise();
            return await Promise.resolve(response.json());
        } catch (error) {
            return await Promise.resolve(error.json());
        }
    }
}
