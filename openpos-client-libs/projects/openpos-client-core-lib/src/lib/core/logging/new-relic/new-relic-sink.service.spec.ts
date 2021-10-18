import { fakeAsync, tick, discardPeriodicTasks, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';

import { NewRelicLoggerConfig, NewRelicMessageGroup, NewRelicSink } from './new-relic-sink.service';
import { ConsoleMessage, ConsoleScraper } from '../console-scraper.service';
import { ConfigurationService } from '../../services/configuration.service';
import { PersonalizationService } from '../../personalization/personalization.service';
import { SessionService } from '../../services/session.service';
import { MessageTypes } from '../../messages/message-types';
import { CapacitorService } from '../../services/capacitor.service';

describe('NewRelic Sink', () => {
    let messages: Subject<ConsoleMessage>;
    let configurationService: jasmine.SpyObj<ConfigurationService>;
    let personalizationService: jasmine.SpyObj<PersonalizationService>;
    let screenMessages: Subject<{ type: string, screenType: string }>;
    let capacitorService: jasmine.SpyObj<CapacitorService>;

    let configuration: Subject<NewRelicLoggerConfig>;

    let fixture: NewRelicSink;

    let deviceId: BehaviorSubject<string | null>;
    let appId: BehaviorSubject<string | null>;
    let serverName: BehaviorSubject<string | null>;
    let serverPort: BehaviorSubject<string | null>;

    let httpController: HttpTestingController;

    beforeEach(() => {
        messages = new Subject<ConsoleMessage>();
        screenMessages = new Subject<{ type: string, screenType: string }>();

        const scraper = {
            messages$: messages
        };

        const sessionService = {
            screenMessage$: screenMessages
        };

        configuration = new Subject<NewRelicLoggerConfig>();

        configurationService = jasmine.createSpyObj<ConfigurationService>('TestConfigService', ['getConfiguration']);
        configurationService.getConfiguration.and.returnValue(configuration);

        personalizationService = jasmine.createSpyObj<PersonalizationService>('TestConfigService', ['getAppId$', 'getDeviceId$', 'getServerName$', 'getServerPort$']);
        capacitorService = jasmine.createSpyObj<CapacitorService>('TestCapacitorService', ['isRunningInCapacitor', 'getDeviceName']);

        deviceId = new BehaviorSubject<string | null>(null);
        appId = new BehaviorSubject<string | null>(null);
        serverName = new BehaviorSubject<string | null>(null);
        serverPort = new BehaviorSubject<string | null>(null);

        personalizationService.getAppId$.and.returnValue(appId);
        personalizationService.getDeviceId$.and.returnValue(deviceId);
        personalizationService.getServerName$.and.returnValue(serverName);
        personalizationService.getServerPort$.and.returnValues(serverPort);


        TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule
            ],
            providers: [
                { provide: ConsoleScraper, useValue: scraper },
                { provide: ConfigurationService, useValue: configurationService },
                { provide: PersonalizationService, useValue: personalizationService },
                { provide: SessionService, useValue: sessionService },
                { provide: CapacitorService, useValue: capacitorService },
                NewRelicSink
            ]
        }).compileComponents();

        fixture = TestBed.inject(NewRelicSink);
        httpController = TestBed.inject(HttpTestingController);
    });

    it('creates', fakeAsync(() => {
        const config = new NewRelicLoggerConfig();
        config.apiKey = 'TESTAPIKEY';
        config.enabled = true;

        configuration.next(config);

        capacitorService.isRunningInCapacitor.and.returnValue(true);
        capacitorService.getDeviceName.and.returnValue(of('TestDevice'));

        deviceId.next('00001-001');
        appId.next('test');
        serverName.next('test.server');
        serverPort.next('12345');
        screenMessages.next({ type: MessageTypes.SCREEN, screenType: 'testscreen' });

        messages.next({
            level: 'info',
            message: 'testing'
        });

        tick(2500);

        const req = httpController.expectOne('https://log-api.newrelic.com/log/v1');

        expect(req.request.headers.get('Api-Key')).toEqual('TESTAPIKEY');
        expect(req.request.headers.get('Content-Type')).toEqual('application/json');

        const payload = req.request.body as NewRelicMessageGroup[];

        expect(payload.length).toBe(1);

        const gp = payload[0];

        expect(gp.logs.length).toBe(1);
        expect(gp.logs[0].message).toEqual('testing');
        expect(gp.logs[0].log_level).toEqual('info');
        expect(gp.logs[0].app_id).toEqual('test');
        expect(gp.logs[0].device_id).toEqual('00001-001');
        expect(gp.logs[0].server_name).toEqual('test.server');
        expect(gp.logs[0].server_port).toEqual('12345');
        expect(gp.logs[0].screen).toEqual('testscreen');

        discardPeriodicTasks();
    }));
});
