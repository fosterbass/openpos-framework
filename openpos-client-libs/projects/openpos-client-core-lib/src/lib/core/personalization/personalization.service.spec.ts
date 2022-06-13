import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PersonalizationConfig, PersonalizationParameter } from './personalization-config';
import { PersonalizationResponse } from './personalization-request';
import { PersonalizationService } from './personalization.service';

describe('PersonalizationService', () => {

    let httpMock: HttpTestingController;
    let personalizationService: PersonalizationService;


    function setup() {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [PersonalizationService]
        });

        // Inject the http service and test controller for each test
        httpMock = TestBed.inject(HttpTestingController);
        personalizationService = TestBed.inject(PersonalizationService);
    }

    function cleanup() {
        localStorage.clear();
    }


    describe('personalizeFromSavedSession', () => {
        it('should send personalization request with device token from local storage', () => {

            cleanup();

            localStorage.setItem('deviceToken', 'MY_TOKEN');
            localStorage.setItem('serverName', 'server');
            localStorage.setItem('serverPort', '6140');

            setup();

            personalizationService.personalizeFromSavedSession().subscribe(result => {
                expect(result).toBe('Personalization successful');
                expect(personalizationService.getDeviceId$().getValue()).toBe('11111-111');
                expect(personalizationService.getAppId$().getValue()).toBe('pos');
            });

            const req = httpMock.expectOne('http://server:6140/rest/devices/personalize');

            expect(req.request.method).toBe('POST');

            const resp = {
                authToken: 'MY_TOKEN',
                deviceModel: {
                    deviceId: '11111-111',
                    appId: 'pos'
                }
            } as PersonalizationResponse;

            req.flush(resp);

            httpMock.verify();
        });
        it('should send personalization request with device token from local storage over ssl', () => {

            cleanup();

            localStorage.setItem('deviceToken', 'MY_TOKEN');
            localStorage.setItem('sslEnabled', 'true');
            localStorage.setItem('serverName', 'server');
            localStorage.setItem('serverPort', '6140');

            setup();

            personalizationService.personalizeFromSavedSession().subscribe(result => {
                expect(result).toBe('Personalization successful');
                expect(personalizationService.getDeviceId$().getValue()).toBe('11111-111');
                expect(personalizationService.getAppId$().getValue()).toBe('pos');
            });

            const req = httpMock.expectOne('https://server:6140/rest/devices/personalize');

            expect(req.request.method).toBe('POST');

            const resp = {
                authToken: 'MY_TOKEN',
                deviceModel: {
                    deviceId: '11111-111',
                    appId: 'pos'
                }
            } as PersonalizationResponse;

            req.flush(resp);

            httpMock.verify();
        });

        it('should throw error if there is no cached token', () => {
           cleanup();
           setup();

           personalizationService.personalizeFromSavedSession().subscribe({
               error: error => expect(error).toEqual('No saved session')
           });

           httpMock.expectNone('http://server:6140/rest/devices/personalize');

        });
    });

    describe('personalize', () => {
        it('should send personalization request to server and save response', done => {
            cleanup();
            setup();

            const params = new Map<string, string>();
            params.set('deviceType', 'register');

            personalizationService.personalize({
                serverConnection: { host: 'server', port: 6140, secured: false },
                deviceId: '22222-222',
                appId: 'pos',
                params
            }).subscribe(result => {
                expect(result).toBe('Personalization successful');
                expect(personalizationService.getAppId$().getValue()).toBe('pos');
                expect(personalizationService.getDeviceId$().getValue()).toBe('22222-222');
                expect(personalizationService.getServerName$().getValue()).toBe('server');
                expect(personalizationService.getServerPort$().getValue()).toBe('6140');
                expect(personalizationService.getSslEnabled$().getValue()).toBe(false);
                expect(localStorage.getItem('deviceToken')).toBe('MY_TOKEN');
                expect(localStorage.getItem('serverName')).toBe('server');
                expect(localStorage.getItem('serverPort')).toBe('6140');
                done();
            });

            const req = httpMock.expectOne('http://server:6140/rest/devices/personalize');

            const resp = {
                authToken: 'MY_TOKEN',
                deviceModel: {
                    deviceId: '22222-222',
                    appId: 'pos'
                }
            } as PersonalizationResponse;
            req.flush(resp);
            httpMock.verify();
        });
        it('should send personalization request to server and save response over ssl', () => {
            cleanup();
            setup();

            const params = new Map<string, string>();
            params.set('deviceType', 'register');

            personalizationService.personalize('server', '6140', '22222-222', 'pos', params, true).subscribe(result => {
                expect(result).toBe('Personalization successful');
                expect(personalizationService.getAppId$().getValue()).toBe('pos');
                expect(personalizationService.getDeviceId$().getValue()).toBe('22222-222');
                expect(personalizationService.getServerName$().getValue()).toBe('server');
                expect(personalizationService.getServerPort$().getValue()).toBe('6140');
                expect(personalizationService.getSslEnabled$().getValue()).toBe(true);
                expect(localStorage.getItem('deviceToken')).toBe('MY_TOKEN');
                expect(localStorage.getItem('serverName')).toBe('server');
                expect(localStorage.getItem('serverPort')).toBe('6140');
            });

            const req = httpMock.expectOne('https://server:6140/rest/devices/personalize');

            const resp = {
                authToken: 'MY_TOKEN',
                deviceModel: {
                    deviceId: '22222-222',
                    appId: 'pos'
                }
            } as PersonalizationResponse;

            req.flush(resp);

            httpMock.verify();
        });
    });

    describe('dePersonalize', () => {

        it('should remove all values from local storage', () => {
            setup();

            personalizationService.dePersonalize();
            expect(localStorage.getItem('serverName')).toBeNull();
            expect(localStorage.getItem('serverPort')).toBeNull();
            expect(localStorage.getItem('deviceToken')).toBeNull();
            expect(localStorage.getItem('theme')).toBeNull();
            expect(localStorage.getItem('sslEnabled')).toBeNull();
        });
    });

    describe('requestPersonalizationConfig', () => {

        it('should request personalization config', () => {
            cleanup();
            setup();

            personalizationService.requestPersonalizationConfig('server', '6140', false).subscribe();

            const req = httpMock.expectOne('http://server:6140/rest/devices/personalizationConfig');

            expect(req.request.method).toBe('GET');

            const resp = {
                parameters: [
                    { label: 'Device Type', property: 'deviceType', defaultValue: 'default' } as PersonalizationParameter,
                ]
            } as PersonalizationConfig;

            req.flush(resp);

            httpMock.verify();
        });
        it('should request personalization config over ssl', () => {
            cleanup();
            setup();

            personalizationService.requestPersonalizationConfig('server', '6140', true).subscribe();

            const req = httpMock.expectOne('https://server:6140/rest/devices/personalizationConfig');

            expect(req.request.method).toBe('GET');

            const resp = {
                parameters: [
                    { label: 'Device Type', property: 'deviceType', defaultValue: 'default' } as PersonalizationParameter,
                ]
            } as PersonalizationConfig;

            req.flush(resp);

            httpMock.verify();
        });
    });
});
