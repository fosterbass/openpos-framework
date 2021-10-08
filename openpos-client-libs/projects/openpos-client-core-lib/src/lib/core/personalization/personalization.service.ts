import {Injectable, Injector} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PersonalizationConfigResponse} from './personalization-config-response.interface';
import {BehaviorSubject, combineLatest, concat, Observable, of, pipe, throwError, zip} from 'rxjs';
import {catchError, map, tap, timeout} from 'rxjs/operators';
import {PersonalizationRequest} from './personalization-request';
import {PersonalizationResponse} from './personalization-response.interface';
import {AutoPersonalizationParametersResponse} from "./device-personalization.interface";
import {Configuration} from "../../configuration/configuration";
import {WrapperService} from "../services/wrapper.service";
import { Storage } from '../storage/storage.service';

@Injectable({
    providedIn: 'root'
})
export class PersonalizationService {
    static readonly OPENPOS_MANAGED_SERVER_PROPERTY = 'managedServer';

    readonly personalizationInitialized$ = new BehaviorSubject<boolean>(false);

    private readonly personalizationProperties$ = new BehaviorSubject<Map<string, string> | null>(null);
    private readonly deviceId$ = new BehaviorSubject<string | null>(null);
    private readonly appId$ = new BehaviorSubject<string | null>(null);
    private readonly deviceToken$ = new BehaviorSubject<string | null>(null);
    private readonly serverName$ = new BehaviorSubject<string | null>(null);
    private readonly serverPort$ = new BehaviorSubject<string | null>(null);
    private readonly sslEnabled$ = new BehaviorSubject<boolean | null>(null);
    private readonly isManagedServer$ = new BehaviorSubject<boolean | null>(null);
    private readonly personalizationSuccessFul$ = new BehaviorSubject<boolean>(false);

    constructor(
        private storage: Storage,
        private wrapperService: WrapperService, 
        private http: HttpClient
    ) {
        storage.isAvailable().subscribe( available => {
            console.log(`[PersonalizationService] storage is available, initializing`);
            zip(
                storage.getValue('deviceToken'),
                storage.getValue('serverName'),
                storage.getValue('serverPort'),
                storage.getValue('sslEnabled'),
                storage.getValue(PersonalizationService.OPENPOS_MANAGED_SERVER_PROPERTY)
            ).subscribe(results => {
                this.deviceToken$.next(results[0] !== 'null' ? results[0] : null);
                this.serverName$.next(results[1] !== 'null' ? results[1] : null);
                this.serverPort$.next(results[2] !== 'null' ? results[2] : null);
                this.sslEnabled$.next(results[3] !== 'null' ? results[3] === 'true' : false );
                if (results[4]) {
                    this.isManagedServer$.next(results[4] !== 'null' ? results[4] === 'true' : false);
                }
                this.personalizationInitialized$.next(true);
            });
        });
    }

    public shouldAutoPersonalize(): boolean {
        return this.wrapperService.shouldAutoPersonalize();
    }

    public getAutoPersonalizationParameters(deviceName: string, url: string): Observable<AutoPersonalizationParametersResponse> {
        const protocol = this.sslEnabled$.getValue() ? 'https://' : 'http://';
        url = protocol + url;
        return this.http.get<AutoPersonalizationParametersResponse>(url, { params: { deviceName: deviceName }})
            .pipe(
                timeout(Configuration.autoPersonalizationRequestTimeoutMillis),
                tap(response => {
                    if (response) {
                        console.log('Auto personalization response received.', response);
                        response.sslEnabled = this.sslEnabled$.getValue();
                    }
                }));
    }

    public personalizeFromSavedSession(): Observable<string>{
        const request = new PersonalizationRequest(this.deviceToken$.getValue(), null, null, null );

        console.log('[PersonalizationService] Personalizing from saved session.');
        return this.sendPersonalizationRequest(this.sslEnabled$.getValue(), this.serverName$.getValue(), this.serverPort$.getValue(), request, null);
    }

    public hasSavedSession(): Observable<boolean> {
        return combineLatest(
            this.storage.getValue('deviceToken'),
            this.storage.getValue('serverPort'),
            this.storage.getValue('serverName')

        ).pipe(
            map(([deviceToken, serverPort, serverName]) => {
                console.info(`deviceToken:${deviceToken}, serverPort: ${serverPort}, serverName: ${serverName}`);
                return !!deviceToken && deviceToken !== "null"
                    && !!serverPort && serverPort !== "null"
                    && !!serverName && serverName !== "null";
            })
        );
    }

    public store(
        serverName: string,
        serverPort: string,
        deviceId: string,
        appId: string,
        personalizationProperties?: Map<string, string>,
        sslEnabled?: boolean) {
        this.setServerName(serverName);
        this.setServerPort(serverPort);
        this.setDeviceId(deviceId);
        this.setAppId(appId);
        this.setPersonalizationProperties(personalizationProperties);
        this.setSslEnabled(sslEnabled);
    }

    public personalize(
        serverName: string,
        serverPort: string,
        deviceId: string,
        appId: string,
        personalizationProperties?: Map<string, string>,
        sslEnabled?: boolean): Observable<string> {

        let request = new PersonalizationRequest(this.deviceToken$.getValue(), deviceId, appId, null);
        return this.sendPersonalizationRequest(sslEnabled, serverName, serverPort, request, personalizationProperties);

    }

    private sendPersonalizationRequest(sslEnabled: boolean, serverName: string, serverPort: string, request: PersonalizationRequest, personalizationParameters: Map<string, string>): Observable<string> {
        let url = sslEnabled ? 'https://' : 'http://';
        url += serverName + ':' + serverPort + '/rest/devices/personalize';

        if (personalizationParameters) {
            personalizationParameters.forEach((value, key) => request.personalizationParameters[key] = value);
        }

        return this.http.post<PersonalizationResponse>(url, request).pipe(
            map((response: PersonalizationResponse) => {
                console.info(`personalizing with server: ${serverName}, port: ${serverPort}, deviceId: ${request.deviceId}`);
                this.setServerName(serverName);
                this.setServerPort(serverPort);
                this.setDeviceId(response.deviceModel.deviceId);
                this.setDeviceToken(response.authToken);
                this.setAppId(response.deviceModel.appId);
                if (!personalizationParameters) {
                    personalizationParameters = new Map<string, string>();
                }
                if (response.deviceModel.deviceParamModels) {
                    response.deviceModel.deviceParamModels.forEach(value => personalizationParameters.set(value.paramName, value.paramValue));
                }

                this.setPersonalizationProperties(personalizationParameters);

                if (sslEnabled) {
                    this.setSslEnabled(sslEnabled);
                } else {
                    this.setSslEnabled(false);
                }

                this.personalizationSuccessFul$.next(true);
                return 'Personalization successful';
            }),
            catchError(error => {
                this.personalizationSuccessFul$.next(false);
                if (error.status == 401) {
                    return throwError(`Device saved token does not match server`);
                }

                if (error.status == 0) {
                    return throwError(`Unable to connect to ${serverName}:${serverPort}`);
                }

                return throwError(`${error.statusText}`);
            })
        )
    }

    public refreshApp() {
        window.location.reload();
    }

    public clearStorage(): Observable<void> {
        return concat(
            this.storage.clear(),
            of(this.remove(this.deviceId$, null)),
            of(this.remove(this.serverName$, null)),
            of(this.remove(this.serverPort$, null)),
            of(this.remove(this.deviceToken$, null)),
            of(this.remove(this.sslEnabled$, null)),
            of(this.remove(this.isManagedServer$, null)),
            of(this.remove(this.personalizationProperties$, null))
        );
    }

    public dePersonalize() {
        this.remove(this.deviceId$, null);
        this.remove(this.appId$, null);
        this.remove(this.serverName$, this.storage.remove('serverName'));
        this.remove(this.serverPort$, this.storage.remove('serverPort'));
        this.remove(this.deviceToken$, this.storage.remove('deviceToken'));
        this.remove(null, this.storage.remove('theme'));
        this.remove(this.sslEnabled$, this.storage.remove('sslEnabled'));
        this.remove(this.isManagedServer$, this.storage.remove(PersonalizationService.OPENPOS_MANAGED_SERVER_PROPERTY));
        this.remove(this.personalizationProperties$, null);

    }


    public requestPersonalizationConfig(serverName: string, serverPort: string, sslEnabled: boolean): Observable<PersonalizationConfigResponse> {
        let url = sslEnabled ? 'https://' : 'http://';
        url += serverName + ':' + serverPort + '/rest/devices/personalizationConfig';

        console.log('Requesting Personalization config with url: ' + url);
        return this.http.get<PersonalizationConfigResponse>(url).pipe(
            tap(result => result ? console.log('Successful retrieval of Personalization Config with url: ' + url) : null)
        );
    }

    public getPersonalizationProperties$(): BehaviorSubject<Map<string, string>> {
        return this.personalizationProperties$;
    }

    public getSslEnabled$(): BehaviorSubject<boolean> {
        return this.sslEnabled$;
    }

    public getServerName$(): BehaviorSubject<string> {
        return this.serverName$;
    }

    public getServerPort$(): BehaviorSubject<string> {
        return this.serverPort$;
    }

    public getDeviceId$(): BehaviorSubject<string> {
        return this.deviceId$;
    }

    public getAppId$(): BehaviorSubject<string> {
        return this.appId$;
    }

    public getDeviceToken$(): BehaviorSubject<string> {
        return this.deviceToken$;
    }

    public getIsManagedServer$(): BehaviorSubject<boolean> {
        return this.isManagedServer$;
    }

    public getPersonalizationSuccessful$(): BehaviorSubject<boolean> {
        return this.personalizationSuccessFul$;
    }

    private setPersonalizationProperties(personalizationProperties?: Map<string, string>) {
        this.personalizationProperties$.next(personalizationProperties);
    }

    private setSslEnabled(enabled: boolean) {
        this.storage.setValue('sslEnabled', enabled + '').subscribe();
        this.sslEnabled$.next(enabled);
    }

    private setServerName(name: string) {
        this.storage.setValue('serverName', name).subscribe();
        this.serverName$.next(name);
    }

    private setServerPort(port: string) {
        this.storage.setValue('serverPort', port).subscribe();
        this.serverPort$.next(port);
    }

    private setDeviceId(id: string) {
        this.deviceId$.next(id);
    }

    private setAppId(id: string) {
        this.appId$.next(id);
    }

    private setDeviceToken(token: string){
        this.storage.setValue('deviceToken', token).subscribe();
        this.deviceToken$.next(token);
    }

    private remove(fieldSubject: BehaviorSubject<any>, storageField: Observable<void>) {
        if (fieldSubject) {
            fieldSubject.next(null);
        }
        if (storageField) {
            storageField.subscribe();
        }
    }

}
