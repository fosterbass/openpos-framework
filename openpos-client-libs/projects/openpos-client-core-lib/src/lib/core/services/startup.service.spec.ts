import { TestBed } from '@angular/core/testing';
import { StartupService, STARTUP_TASKS, STARTUP_COMPONENT, STARTUP_FAILED_COMPONENT, STARTUP_FAILED_TASK } from './startup.service';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { IStartupTask } from '../startup/startup-task.interface';
import { scan } from 'rxjs/operators';
import { cold, getTestScheduler } from 'jasmine-marbles';
import { AppInjector } from '../app-injector';
import { Injector } from '@angular/core';
import { StartupComponent } from '../startup/startup.component';
import { StartupFailedComponent } from '../startup/startup-failed.component';
import { Subscription } from 'rxjs';

describe('StartupService', () => {

    let startupService: StartupService;
    let matDialog: jasmine.SpyObj<MatDialog>;
    let matDialogRef: jasmine.SpyObj<MatDialogRef<any>>;
    let resultMessages = '';
    let result: boolean;
    let startupTaskProviders: any[];
    let subscriptions!: Subscription;

    function addTask(type: IStartupTask) {
        startupTaskProviders.push({ provide: STARTUP_TASKS, useValue: type, multi: true });
    }

    async function setup() {
        const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
        const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close', 'afterClosed']);
        const loggerSpy = jasmine.createSpyObj('Logger', ['info', 'error']);
        matDialogRef = matDialogRefSpy;

        await TestBed.configureTestingModule({
            providers: [
                StartupService,
                { provide: STARTUP_COMPONENT, useValue: StartupComponent },
                { provide: STARTUP_FAILED_COMPONENT, useValue: StartupFailedComponent },
                {
                    provide: STARTUP_FAILED_TASK, useValue:
                        { name: 'FailureTask', order: 100, execute: () => cold('--x|', { x: 'FailureTask' }) }
                },
                { provide: MatDialog, useValue: matDialogSpy },
                ...startupTaskProviders
            ]
        }).compileComponents();

        AppInjector.Instance = TestBed.inject(Injector);
        startupService = TestBed.inject(StartupService);
        matDialog = TestBed.inject(MatDialog) as jasmine.SpyObj<MatDialog>;
        matDialog.open.and.returnValue(matDialogRefSpy);

        // never complete so we don't restart
        matDialogRef.afterClosed.and.returnValue(cold('---', { x: null }));

        // collect all the messages
        subscriptions = startupService.startupTaskMessages$.pipe(
            scan((acc, curr) => acc + curr, ''))
            .subscribe((messages) => resultMessages = messages);


        // collect the result of the route activation
        subscriptions.add(startupService.canActivate(null, null).subscribe((r) => {
            result = r;
        }));
    }

    beforeEach(() => {
        if (subscriptions) {
            subscriptions.unsubscribe();
            subscriptions = undefined;
        }

        startupTaskProviders = [];
        resultMessages = '';
    });

    describe('canActivate', () => {
        it('should return true if there are no tasks', async () => {
            expect(startupTaskProviders.length).toBe(0);

            // do the test module setup
            await setup();

            getTestScheduler().flush();

            expect(matDialog.open).not.toHaveBeenCalled();
            expect(resultMessages).toBe('');
            expect(result).toBeTruthy();
        });

        it('should run all deduped tasks in order and return true', async () => {
            // add tasks
            addTask({ name: 'TestTask1', order: 100, execute: () => cold('--x|', { x: 'Test1' }) });
            addTask({ name: 'TestTask2', order: 1, execute: () => cold('--x|', { x: 'Test2' }) });
            addTask({ name: 'TestTask1', order: 11, execute: () => cold('--x|', { x: 'Test3' }) });

            // do the test module setup
            await setup();

            getTestScheduler().flush();

            expect(matDialog.open).toHaveBeenCalledWith(StartupComponent, jasmine.anything());
            expect(matDialogRef.close).toHaveBeenCalledTimes(1);
            expect(resultMessages).toBe('TestTask2: Test2TestTask1: Test3');
            expect(result).toBeTruthy();
        });

        it('should not run any tasks after one fails', async () => {
            // add tasks
            addTask({ name: 'TestTask1', order: 100, execute: () => cold('--x|', { x: 'Test1' }) });
            addTask({ name: 'TestTask2', order: 1, execute: () => cold('--x|', { x: 'Test2' }) });
            addTask({ name: 'TestTask3', order: 11, execute: () => cold('--#|', null, new Error('Test3Failed')) });

            await setup();

            getTestScheduler().flush();

            expect(result).toBeFalsy();
            expect(resultMessages).toBe('TestTask2: Test2FailureTask');
            expect(matDialog.open).toHaveBeenCalledWith(StartupComponent, jasmine.anything());
            expect(matDialogRef.close).toHaveBeenCalledTimes(1);
            expect(matDialog.open).toHaveBeenCalledWith(StartupFailedComponent, {
                width: jasmine.anything(),
                height: jasmine.anything(),
                disableClose: jasmine.anything(),
                hasBackdrop: jasmine.anything(),
                data: {
                    error: 'TestTask3: Error: Test3Failed',
                    messages: ['TestTask2: Test2', 'FailureTask']
                },
                panelClass: jasmine.anything()
            });
        });
    });

});
