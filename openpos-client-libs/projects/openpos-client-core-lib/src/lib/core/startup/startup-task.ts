import { Observable } from 'rxjs';
import { InjectionToken, Injector, Type } from '@angular/core';
import { SplashScreen } from '../services/splash-screen.service';

export interface StartupTask {
    execute(): void | Promise<void> | Observable<any>;
}

export abstract class StartupTaskCombinator {
    abstract executeTask(injector: Injector): Promise<void>;

    protected async performTask(injector: Injector, task: StartupTaskLike): Promise<boolean> {
        const name = getTaskName(task);

        const endConsoleScope = ConsoleLogAugmenter.beginScope(name);

        try {
            if (isCombinator(task)) {
                try {
                    await task.executeTask(injector);
                } catch (e) {
                    return false;
                }
            } else {
                const start = performance.now();

                try {
                    console.log(`begin startup task '${name}'`);

                    const value = injector.get(task);

                    if (value) {
                        try {
                            const result = value.execute();

                            if (result) {
                                if (isPromise(result)) {
                                    console.debug(`startup task '${name}' is promise; awaiting`);
                                    await result;
                                } else if (isObservable(result)) {
                                    console.debug(`startup task '${name}' is observable; waiting for completion`);
                                    await result.toPromise();
                                    // todo: use this in later versions of rxjs: await lastValueFrom(result, { defaultValue: {} });
                                }
                            }
                        } catch (e) {
                            console.error('failed to complete startup task due to a thrown error', e);
                            return false;
                        }
                    } else {
                        console.error(`failed to create task '${name}'; injector failed`);
                        return false;
                    }
                } finally {
                    const delta = performance.now() - start;
                    console.log(`completed startup task '${name}'; took ${makeTimeString(delta)}`);
                }
            }
        } finally {
            endConsoleScope();
        }

        return true;
    }
}

export type StartupTaskLike = Type<StartupTask> | InjectionToken<StartupTask> | StartupTaskCombinator;

class AllStartupTaskCombinator extends StartupTaskCombinator {
    constructor(private _tasks: StartupTaskLike[]) {
        super();
    }

    async executeTask(injector: Injector): Promise<void> {
        for (const task of this._tasks) {
            const result = await super.performTask(injector, task);

            if (!result) {
                throw new Error('task failed to execute');
            }
        }
    }
}

class OneOfStartupTaskCombinator extends StartupTaskCombinator {
    constructor(private _tasks: StartupTaskLike[]) {
        super();
    }

    async executeTask(injector: Injector): Promise<void> {
        if (this._tasks.length === 0) {
            console.debug('no tasks for oneOf combinator to perform; bypassing...');
            return;
        }

        for (const task of this._tasks) {
            if (await super.performTask(injector, task)) {
                console.debug(`oneOf task '${getTaskName(task)}' completed successfully; continuing...`);
                return;
            }

            console.debug(`oneOf task '${getTaskName(task)}' failed during execution; trying next task`);
        }

        throw new Error('all tasks failed');
    }
}

class OptionalStartupTaskCombinator extends StartupTaskCombinator {
    constructor(private _task: StartupTaskLike) {
        super();
    }

    async executeTask(injector: Injector): Promise<void> {
        if (!await super.performTask(injector, this._task)) {
            console.debug(`startup task '${getTaskName(this._task)}' failed to execute; ignoring optional task`);
        }
    }
}

class WithSplashMessageTaskCombinator extends StartupTaskCombinator {
    constructor(private _splashMessage: string, private _task: StartupTaskLike) {
        super();
    }

    async executeTask(injector: Injector): Promise<void> {
        const splashScreen = injector.get(SplashScreen);

        if (!splashScreen) {
            console.warn('could not locate splash screen service; splash messages will not be displayed');
        }

        const splashRef = splashScreen.pushMessage(this._splashMessage);

        try {
            if (!await super.performTask(injector, this._task)) {
                throw new Error('contained task failed execution');
            }
        } finally {
            splashRef.pop();
        }
    }
}

export function all(...tasks: StartupTaskLike[]): StartupTaskCombinator {
    return new AllStartupTaskCombinator(tasks);
}

export function oneOf(...tasks: StartupTaskLike[]): StartupTaskCombinator {
    return new OneOfStartupTaskCombinator(tasks);
}

export function optional(task: StartupTaskLike): StartupTaskCombinator {
    return new OptionalStartupTaskCombinator(task);
}

export function withSplashMessage(message: string, task: StartupTaskLike): StartupTaskCombinator {
    return new WithSplashMessageTaskCombinator(message, task);
}

function getTaskName(task: StartupTaskLike): string {
    function hasName(inst: any): inst is { taskName: string } {
        return !!inst.taskName && typeof inst.taskName === 'string';
    }

    if (hasName(task)) {
        return task.taskName;
    } else {
        return 'UnnamedStartupTask';
    }
}

function isCombinator(inst: any): inst is StartupTaskCombinator {
    return inst instanceof StartupTaskCombinator;
}

function isPromise(inst: any): inst is Promise<any> {
    return inst.then && inst.catch;
}

function isObservable(inst: any): inst is Observable<any> {
    return inst instanceof Observable;
}

function makeTimeString(milliseconds: number): string {
    if (milliseconds < 1) {
        const micros = Math.round(milliseconds * 1000);
        return `${micros}μs`;
    } else {
        const hours = Math.floor(milliseconds / 3600000);
        const minutes = Math.floor((milliseconds - (hours * 3600000)) / 60000);
        const seconds = Math.floor((milliseconds - (hours * 3600000) - (minutes * 60000)) / 1000);
        milliseconds = Math.round(milliseconds - (hours * 3600000) - (minutes * 60000) - (seconds * 1000));

        const showHours = hours > 0;
        const showMins = showHours || minutes > 0;
        const showSec = showMins || seconds > 0;

        let result = '';

        if (showHours) {
            result += `${hours}h `;
        }

        if (showMins) {
            result += `${minutes}m `;
        }

        if (showSec) {
            result += `${seconds}s `;
        }

        result += `${milliseconds}ms`;

        return result;
    }
}

type SupportedConsoleMethod = keyof Console & ('info' | 'debug' | 'error' | 'warn' | 'log');

class ConsoleLogAugmenter {
    private static _interceptDepth = 0;
    private static _consoleMethods?: { [Property in SupportedConsoleMethod]: (...args: any[]) => void };

    private constructor() {
    }

    static beginScope(name: string): () => void {
        if (this._interceptDepth < 1) {
            ConsoleLogAugmenter._consoleMethods = {
                info: console.info,
                log: console.log,
                error: console.error,
                warn: console.warn,
                debug: console.debug,
            };
        }

        ConsoleLogAugmenter._interceptDepth += 1;

        const stopInfo = this._hookConsole(name, 'info');
        const stopDebug = this._hookConsole(name, 'debug');
        const stopError = this._hookConsole(name, 'error');
        const stopWarn = this._hookConsole(name, 'warn');
        const stopLog = this._hookConsole(name, 'log');

        return () => {
            stopInfo();
            stopDebug();
            stopError();
            stopWarn();
            stopLog();

            this._interceptDepth -= 1;

            if (this._interceptDepth < 1) {
                ConsoleLogAugmenter._consoleMethods = undefined;
            }
        };
    }

    private static _hookConsole(name: string, method: SupportedConsoleMethod): () => void {
        const original = console[method];

        console[method] = (...args: any[]) => {
            if (args.length > 0) {
                const newArgList = new Array<any>();

                let firstArg = args[0];

                if (typeof firstArg === 'string') {
                    firstArg = `[${name}] ${firstArg}`;
                    newArgList.push(firstArg);
                } else {
                    newArgList.push(`[${name}]`);
                    newArgList.push(firstArg);
                }

                args.slice(1).forEach(arg => newArgList.push(arg));

                ConsoleLogAugmenter._consoleMethods[method](...newArgList);
            }
        };

        return () => {
            console[method] = original;
        };
    }
}
