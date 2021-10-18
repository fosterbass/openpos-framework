import { CONFIGURATION } from './../../configuration/configuration';
import { ConfigurationService } from './configuration.service';
import { SessionService } from './session.service';
import { TestBed } from '@angular/core/testing';
import { cold, getTestScheduler } from 'jasmine-marbles';
import { ElectronService } from 'ngx-electron';

describe('ConfigurationService', () => {

    let configService: ConfigurationService;
    let sessionServiceSpy: jasmine.SpyObj<SessionService>;
    const testUiConfigMsgTemplate = {
        type : 'ConfigChanged',
        configType : 'uiConfig'
    };

    let testUiConfigMsg = testUiConfigMsgTemplate;

    function getUiConfigMessage() {
        return cold('-x', {x: testUiConfigMsg});
    }

    beforeEach(() => {
        testUiConfigMsg = JSON.parse(JSON.stringify(testUiConfigMsgTemplate));
        const sessionSpy = jasmine.createSpyObj('SessionService', ['getMessages']);
        TestBed.configureTestingModule({
            providers: [
                ElectronService,
                { provide: SessionService, useValue: sessionSpy },
            ]
        });
        sessionServiceSpy = TestBed.inject(SessionService) as jasmine.SpyObj<SessionService>;
        sessionServiceSpy.getMessages.and.callFake(getUiConfigMessage);

        configService = TestBed.inject(ConfigurationService);

    });

    it('test mapping of response string property to CONFIGURATION boolean property', () => {
        const originalValue = CONFIGURATION.mimicScroll;
        CONFIGURATION.mimicScroll = false;
        // tslint:disable-next-line:no-string-literal
        testUiConfigMsg['mimicScroll'] = 'true';

        getTestScheduler().flush();
        expect(CONFIGURATION.mimicScroll).toEqual(true);
        CONFIGURATION.mimicScroll = originalValue;
    });

    it('test mapping of response boolean property to CONFIGURATION string property', () => {
        const originalValue = CONFIGURATION.compatibilityVersion;
        CONFIGURATION.compatibilityVersion = 'false';
        // tslint:disable-next-line:no-string-literal
        testUiConfigMsg['compatibilityVersion'] = true;

        getTestScheduler().flush();
        expect(CONFIGURATION.compatibilityVersion).toEqual('true');
        CONFIGURATION.compatibilityVersion = originalValue;
    });

    it('test mapping of response string property to CONFIGURATION number property', () => {
        const originalValue = CONFIGURATION.keepAliveMillis;
        CONFIGURATION.keepAliveMillis = 1000;
        // tslint:disable-next-line:no-string-literal
        testUiConfigMsg['keepAliveMillis'] = '1000';

        getTestScheduler().flush();
        expect(CONFIGURATION.keepAliveMillis).toEqual(1000);
        CONFIGURATION.keepAliveMillis = originalValue;
    });

    it('test mapping of response number property to CONFIGURATION string property', () => {
        const originalValue = CONFIGURATION.compatibilityVersion;
        CONFIGURATION.compatibilityVersion = '2';
        // tslint:disable-next-line:no-string-literal
        testUiConfigMsg['compatibilityVersion'] = 1;

        getTestScheduler().flush();
        expect(CONFIGURATION.compatibilityVersion).toEqual('1');
        CONFIGURATION.compatibilityVersion = originalValue;
    });

    it('test mapping of response string property to CONFIGURATION string property', () => {
        const originalValue = CONFIGURATION.compatibilityVersion;
        CONFIGURATION.compatibilityVersion = '2';
        // tslint:disable-next-line:no-string-literal
        testUiConfigMsg['compatibilityVersion'] = '1';

        getTestScheduler().flush();
        expect(CONFIGURATION.compatibilityVersion).toEqual('1');
        CONFIGURATION.compatibilityVersion = originalValue;
    });

    it('test mapping of invalid response string property to CONFIGURATION number property', () => {
        const originalValue = CONFIGURATION.mimicScroll;
        CONFIGURATION.mimicScroll = false;
        const propValueBeforeTest = CONFIGURATION.keepAliveMillis;
        // tslint:disable-next-line:no-string-literal
        testUiConfigMsg['keepAliveMillis'] = 'foo';

        getTestScheduler().flush();
        expect(CONFIGURATION.keepAliveMillis).toEqual(propValueBeforeTest);
        CONFIGURATION.mimicScroll = originalValue;
    });

    it('test mapping of invalid response string property to CONFIGURATION boolean property', () => {
        const originalValue = CONFIGURATION.mimicScroll;
        CONFIGURATION.mimicScroll = false;
        const propValueBeforeTest = CONFIGURATION.mimicScroll;
        // tslint:disable-next-line:no-string-literal
        testUiConfigMsg['mimicScroll'] = 'foo';

        getTestScheduler().flush();
        expect(CONFIGURATION.mimicScroll).toEqual(propValueBeforeTest);
        CONFIGURATION.mimicScroll = originalValue;
    });
});
