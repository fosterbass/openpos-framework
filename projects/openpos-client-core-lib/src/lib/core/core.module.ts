import { DevMenuComponent } from './components/dev-menu/dev-menu.component';
import { SessionService } from './services/session.service';
import { PersonalizationStartupTask } from './components/startup/personalization-startup-task';
import { STARTUP_TASKS, STARTUP_COMPONENT, STARTUP_FAILED_COMPONENT } from './services/startup.service';

// Angular Includes
import { NgModule, Injector, Optional, SkipSelf } from '@angular/core';
import { Location, LocationStrategy, PathLocationStrategy, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BreakpointObserver, MediaMatcher } from '@angular/cdk/layout';

import { SharedModule } from '../shared/shared.module';
import { AppInjector } from './app-injector';

import { throwIfAlreadyLoaded } from './module-import-guard';
import { StartupComponent } from './components/startup/startup.component';
import { PersonalizationService } from './services/personalization.service';
import { ConfigurationService } from './services/configuration.service';
import { DialogService } from './services/dialog.service';
import { StompRService } from '@stomp/ng2-stompjs';
import { SubscribeToSessionTask } from './components/startup/subscribe-to-session-task';
import { Router, RouterModule } from '@angular/router';
import { Logger } from './services/logger.service';
import { StartupFailedComponent } from './components/startup/startup-failed.component';
import { MatDialog } from '@angular/material';
import { FinalStartupTask } from './components/startup/final-startup-task';
import { DialogContentComponent } from './components/dialog-content/dialog-content.component';
import { DialogServiceStartupTask } from './components/startup/dialog-service-startup-task';
import { TrainingOverlayService } from './services/training-overlay.service';
import { KeyPressProvider } from '../shared/providers/keypress.provider';
import { fromEvent, Observable } from 'rxjs';
import { ConfirmationDialogComponent } from './components/confirmation-dialog/confirmation-dialog.component';
import { PersonalizationComponent } from './components/personalization/personalization.component';
import { OpenposAppComponent } from './components/openpos-app/openpos-app.component';
import { DynamicScreenComponent } from './components/dynamic-screen/dynamic-screen.component';
import { LoaderComponent } from './components/loader/loader.component';
import { StatusBarStatusControlComponent } from '../shared/components/status-bar-status-control/status-bar-status-control.component';
import { STATUS_BAR_STATUS_CONTROL_COMPONENT } from '../shared/components/status-bar/status-bar.component';
import { ToastService } from './services/toast.service';
@NgModule({
    entryComponents: [
        ConfirmationDialogComponent,
        PersonalizationComponent,
        StartupComponent,
        StartupFailedComponent,
        DialogContentComponent,
        StatusBarStatusControlComponent
    ],
    declarations: [
        OpenposAppComponent,
        DynamicScreenComponent,
        DevMenuComponent,
        DialogContentComponent,
        LoaderComponent,
        ConfirmationDialogComponent,
        PersonalizationComponent,
        StartupComponent,
        StartupFailedComponent,
        StatusBarStatusControlComponent
    ],
    imports: [
        SharedModule,
        RouterModule
    ],
    exports: [
        LoaderComponent,
        OpenposAppComponent
    ],
    providers: [
        HttpClient,
        Location,
        { provide: LocationStrategy, useClass: PathLocationStrategy },
        DatePipe,
        BreakpointObserver,
        MediaMatcher,
        StompRService,
        { provide: STARTUP_TASKS, useClass: PersonalizationStartupTask, multi: true, deps: [PersonalizationService, MatDialog]},
        { provide: STARTUP_TASKS, useClass: SubscribeToSessionTask, multi: true, deps: [SessionService, Router, Logger]},
        { provide: STARTUP_TASKS, useClass: DialogServiceStartupTask, multi: true, deps: [DialogService]},
        { provide: STARTUP_TASKS, useClass: FinalStartupTask, multi: true, deps: [SessionService]},
        { provide: STARTUP_COMPONENT, useValue: StartupComponent },
        { provide: STARTUP_FAILED_COMPONENT, useValue: StartupFailedComponent},
        TrainingOverlayService,
        ConfigurationService,
        KeyPressProvider,
        { provide: STATUS_BAR_STATUS_CONTROL_COMPONENT, useValue: StatusBarStatusControlComponent }
    ]
})
export class CoreModule {

    constructor(@Optional() @SkipSelf() parentModule: CoreModule,
                private injector: Injector,
                toastService: ToastService,
                keyProvider: KeyPressProvider) {
        throwIfAlreadyLoaded(parentModule, 'CoreModule');
        AppInjector.Instance = this.injector;
        keyProvider.registerKeyPressSource(fromEvent(document, 'keydown') as Observable<KeyboardEvent>);
        keyProvider.registerKeyPressSource(fromEvent(document, 'keyup') as Observable<KeyboardEvent>);
    }
}
