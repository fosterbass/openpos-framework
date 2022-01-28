import { Injectable } from '@angular/core';
import { Observable, Subscriber } from 'rxjs';
import { publish, refCount } from 'rxjs/operators';

export type LogLevel = 'info' | 'warn' | 'error' | 'debug';
export interface ConsoleMessage {
    level: LogLevel;
    message: string;
}

export type SupportedConsoleMethods = keyof Console & ('info' | 'debug' | 'error' | 'warn' | 'log');

@Injectable({ providedIn: 'root' })
export class ConsoleScraper {
    readonly messages$: Observable<ConsoleMessage>;

    // used to avoid rentraint console logs.
    private _isHandling = false;

    constructor() {
        this.messages$ = new Observable(observer => {
            const hooks = [
                this._hookConsoleMethod('debug', (args) => this._handleLogMessage(observer, 'debug', ...args)),
                this._hookConsoleMethod('info', (args) => this._handleLogMessage(observer, 'info', ...args)),
                this._hookConsoleMethod('warn', (args) => this._handleLogMessage(observer, 'warn', ...args)),
                this._hookConsoleMethod('error', (args) => this._handleLogMessage(observer, 'error', ...args)),
                this._hookConsoleMethod('log', (args) => this._handleLogMessage(observer, 'info', ...args)),
            ];

            return () => {
                hooks.forEach(unhook => unhook());
            };
        }).pipe(
            publish(),
            refCount()
        );
    }

    logBypassHook(method: SupportedConsoleMethods, message: string, ...data: any[]) {
        const oldValue = this._isHandling;

        try {
            this._isHandling = true;
            console[method](message, data);
        } finally {
            this._isHandling = oldValue;
        }
    }

    private _handleLogMessage(observer: Subscriber<ConsoleMessage>, level: LogLevel, ...args: any[]) {
        if (!args || args.length <= 0) {
            return;
        }

        let message = '';

        args.forEach(arg => {
            if (arg !== null && arg !== undefined) {
                if (typeof arg === 'object') {
                    const stringify = this._jsonStringifySanitize(arg);

                    if (stringify.length > 0) {
                        if (message.length > 0) {
                            message += '; ';
                        }

                        message += stringify;
                    }
                } else {
                    if (message.length > 0) {
                        message += '; ';
                    }

                    message += arg;
                }
            }
        });

        if (message.length > 0) {
            observer.next({
                level,
                message,
            });
        }
    }

    private _hookConsoleMethod(method: SupportedConsoleMethods, onlog: (...args: any[]) => void): () => void {
        const originalMethod = console[method];

        const hookfn = (...args: any[]) => {
            if (!this._isHandling) {
                try {
                    this._isHandling = true;
                    onlog(args);
                } catch (e) {
                    console.error(`failed to handle log for method ${method}...`, e);
                } finally {
                    this._isHandling = false;
                }
            }

            originalMethod(...args);
        };

        console[method] = hookfn;

        return () => {
            console[method] = originalMethod;
        };
    }

    private _makeCircularRefReplacer = () => {
        return (key: string, value: any) => {
            if (!!key && typeof value === 'object' && value !== null) {
                return '[Object object]';
            }

            return value;
        };
    }

    // Stringifys given message and handles any circular object references,
    // preventing the JSON.stringify circular reference error
    private _jsonStringifySanitize(message: any): string {
        let cleansed = message;

        if (message && typeof message === 'object') {
            try {
                cleansed = JSON.stringify(message, this._makeCircularRefReplacer());
            } catch (e) {
                cleansed =
                    `Failed to convert object to a string for logging. Reason: ${e && e.hasOwnProperty('message') ? e.message : '?'}`;
            }
        }

        return cleansed;
    }
}
