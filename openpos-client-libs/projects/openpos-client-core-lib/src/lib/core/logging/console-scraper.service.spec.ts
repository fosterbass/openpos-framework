import { Subscription } from 'rxjs';
import { ConsoleMessage, ConsoleScraper, LogLevel, SupportedConsoleMethods } from './console-scraper.service';

describe('ConsoleScraperService', () => {
    function methodToLogLevel(method: SupportedConsoleMethods): LogLevel {
        let logLevel: LogLevel;

        switch (method) {
            case 'log':
            case 'info':
                logLevel = 'info';
                break;

            case 'debug':
                logLevel = 'debug';
                break;

            case 'warn':
                logLevel = 'warn';
                break;

            case 'error':
                logLevel = 'error';
                break;
        }

        return logLevel;
    }

    const supportedMethods: Array<SupportedConsoleMethods> = [
        'debug',
        'error',
        'info',
        'log',
        'warn'
    ];

    const originalConsoleMethods = {};

    supportedMethods.forEach(method => {
        originalConsoleMethods[method] = console[method];
    });

    let scraper: ConsoleScraper | undefined;
    let messageSub: Subscription | undefined;

    let lastMessage: ConsoleMessage | undefined;

    beforeEach(() => {
        scraper = new ConsoleScraper();

        messageSub = scraper.messages$.subscribe(message => {
            lastMessage = message;
        });

        // confirm the console methods were hooked
        supportedMethods.forEach(method => {
            expect(console[method]).not.toBe(originalConsoleMethods[method]);
        });
    });

    afterEach(() => {
        if (messageSub) {
            messageSub.unsubscribe();
            messageSub = undefined;
        }

        scraper = undefined;
        lastMessage = undefined;

        // confirm that unsubscribing restores the console methods.
        supportedMethods.forEach(method => {
            expect(console[method]).toBe(originalConsoleMethods[method]);
        });
    });

    describe('messaging', () => {
        function assertMessage(expected: string, ...consoeArgs: any[]) {
            supportedMethods.forEach(method => {
                console[method](...consoeArgs);

                expect(lastMessage.level).toBe(methodToLogLevel(method));
                expect(lastMessage.message).toBe(expected);
            });
        }


        it('relays basic console message at appropriate level', () => {
            assertMessage('test', 'test');
        });

        it('allows for objects as arguments', () => {
            assertMessage(`{"test":42}`, { test: 42 });
        });

        it('serializes multiple arguments', () => {
            assertMessage(`test; {"test":42}`, 'test', { test: 42 });
        });

        it('handles no argument values', () => {
            supportedMethods.forEach(method => {
                console[method]();

                expect(lastMessage).toBeUndefined();
            });
        });

        it('handles null arguments', () => {
            supportedMethods.forEach(method => {
                console[method](null);
                expect(lastMessage).toBeUndefined();
            });
        });

        it('handles undefined arguments', () => {
            supportedMethods.forEach(method => {
                console[method](undefined);
                expect(lastMessage).toBeUndefined();
            });
        });

        it('ignores null arguments', () => {
            assertMessage('test; something', 'test', null, 'something');
        });

        it('ignores undefined arguments', () => {
            assertMessage('test; something', 'test', undefined, 'something');
        });
    });
});
