import { ConfigurationService } from '../../../core/services/configuration.service';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Renderer2, ElementRef } from '@angular/core';
import { Component, ViewChild, HostListener, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OverlayContainer } from '@angular/cdk/overlay';
import { CONFIGURATION } from '../../../configuration/configuration';
import { PersonalizationService } from '../../../core/personalization/personalization.service';
import { FileViewerComponent } from '../file-viewer/file-viewer.component';
import { IMessageHandler } from '../../../core/interfaces/message-handler.interface';
import { IOldPlugin } from '../../../core/oldplugins/oldplugin.interface';
import { ActionMap } from '../../../core/interfaces/action-map.interface';
import { Element } from '../../../core/interfaces/element.interface';
import { ScreenService } from '../../../core/services/screen.service';
import { DialogService } from '../../../core/services/dialog.service';
import { SessionService } from '../../../core/services/session.service';
import { DeviceService } from '../../../core/services/device.service';
import { IconService } from '../../../core/services/icon.service';
import { OldPluginService } from '../../../core/services/old-plugin.service';
import { FileUploadService } from '../../../core/services/file-upload.service';
import { IVersion } from '../../../core/interfaces/version.interface';
import { Observable } from 'rxjs';
import { DiscoveryService } from '../../../core/discovery/discovery.service';
import { AudioLicense, AudioLicenseLabels } from '../audio-license/audio-license.interface';
import { map } from 'rxjs/operators';
import { KioskModeController } from '../../../core/platform-plugins/kiosk/kiosk-controller.service';

import type { MatExpansionPanel } from '@angular/material/expansion';
import { ElectronPlatform } from '../../../core/platforms/electron.platform';

@Component({
    selector: 'app-dev-menu',
    templateUrl: './dev-menu.component.html',
    styleUrls: ['./dev-menu.component.scss'],
})
export class DevMenuComponent implements OnInit, IMessageHandler<any> {

    static MSG_TYPE = 'DevTools';

    deviceElements: Element[];
    sessionElements: Element[];
    conversationElements: Element[];
    configElements: Element[];
    flowElements: Element[];

    savePoints: string[];

    simAuthToken: string;
    simPort: string;
    simUrl: string;
    simProtocol: string;
    simAuthTokenAvailable = false;

    customerDisplayAuthToken: string;
    customerDisplayPort: string;
    customerDisplayUrl: string;
    customerDisplayProtocol: string;
    customerDisplayPairedDeviceId: string;
    customerDisplayAuthTokenAvailable = false;

    firstClickTime = Date.now();

    clickCount = 0;

    devClicks = 0;

    currentSelectedLogfilename: string;

    logFilenames: string[];

    logPlugin: IOldPlugin;

    showDevMenu = false;

    logsAvailable = false;

    keyCount = 0;

    savePointFileName: string;

    isAutoPersonalizationSupported = this.personalization.getAutoPersonalizationProvider$().pipe(
        map(p => !!p)
    );

    isAutoPersonalizationEnabled = this.personalization.getSkipAutoPersonalization$().pipe(
        map(p => !p)
    );

    autoPersonalizationSettingTouched = false;

    public audioLicenses: AudioLicense[];

    public audioLicenseLabels: AudioLicenseLabels;

    public displaySavePoints = false;

    public showUpdating = false;

    public currentStateActions: ActionMap[];

    public currentStateClass: string;

    public currentState: string;

    public stackTrace: string;

    public selected: string;

    public displayStackTrace = false;

    public classes = '';

    private disableDevMenu = false;

    isInKioskMode = false;

    brand: string;

    @ViewChild('devMenuPanel') devMenuPanel: MatExpansionPanel;

    constructor(
        private personalization: PersonalizationService,
        public screenService: ScreenService,
        public dialogService: DialogService,
        public session: SessionService,
        public deviceService: DeviceService,
        public dialog: MatDialog,
        public iconService: IconService,
        public snackBar: MatSnackBar,
        public overlayContainer: OverlayContainer,
        private pluginService: OldPluginService,
        private fileUploadService: FileUploadService,
        private httpClient: HttpClient,
        private cd: ChangeDetectorRef,
        private elRef: ElementRef,
        public renderer: Renderer2,
        private configurationService: ConfigurationService,
        private discovery: DiscoveryService,
        public kioskMode: KioskModeController,
        private electronPlatform: ElectronPlatform
    ) {

        if (CONFIGURATION.useTouchListener) {
            this.renderer.listen(this.elRef.nativeElement, 'touchstart', (event) => {
                this.documentClick(event);
            });
        }
    }

    handle(message: any) {
        if (message.name === 'DevTools::Get') {
            this.populateDevTables(message);
        }
    }

    async ngOnInit(): Promise<void> {
        const self = this;
        this.session.registerMessageHandler(this, 'DevTools');

        if (this.kioskMode.isKioskModeAvailable) {
            this.isInKioskMode = await this.kioskMode.isInKioskMode();
        }
    }

    private populateDevTables(message: any) {
        this.audioLicenses = message.audioLicenses;
        this.audioLicenseLabels = message.audioLicenseLabels;

        if (message.currentState) {
            console.info('Pulling current state actions...');
            this.currentState = message.currentState.stateName;
            this.currentStateClass = message.currentState.stateClass;
            this.currentStateActions = [];
            for (let i = 0; i < message.actionsSize; i = i + 2) {
                this.currentStateActions.push({
                    Action: message.actions[i],
                    Destination: message.actions[i + 1]

                });
            }
        }
        if (message.scopes) {
            if (message.scopes.ConversationScope) {
                console.info('Pulling Conversation Scope Elements...');
                this.conversationElements = [];
                message.scopes.ConversationScope.forEach(element => {
                    if (!this.conversationElements.includes(element, 0)) {
                        this.conversationElements.push({
                            ID: element.name,
                            Time: element.date,
                            StackTrace: element.stackTrace,
                            Value: element.value
                        });
                    }
                });
            }
            if (message.scopes.SessionScope) {
                console.info('Pulling Session Scope Elements...');
                this.sessionElements = [];
                message.scopes.SessionScope.forEach(element => {
                    if (!this.sessionElements.includes(element, 0)) {
                        this.sessionElements.push({
                            ID: element.name,
                            Time: element.date,
                            StackTrace: element.stackTrace,
                            Value: element.value
                        });
                    }
                });
            }
            if (message.scopes.DeviceScope) {
                console.info('Pulling Device Scope Elements...');
                this.deviceElements = [];
                message.scopes.DeviceScope.forEach(element => {
                    if (!this.deviceElements.includes(element, 0)) {
                        this.deviceElements.push({
                            ID: element.name,
                            Time: element.date,
                            StackTrace: element.stackTrace,
                            Value: element.value
                        });
                    }
                });
            }
            if (message.scopes.FlowScope) {
                console.info('Pulling Flow Scope Elements...');
                this.flowElements = [];
                message.scopes.FlowScope.forEach(element => {
                    if (!this.flowElements.includes(element, 0)) {
                        this.flowElements.push({
                            ID: element.name,
                            Time: element.date,
                            StackTrace: element.stackTrace,
                            Value: element.value
                        });
                    }
                });
                console.info(this.flowElements);
            }

            if (message.scopes.ConfigScope) {
                console.info('Pulling Config Scope Elements...');
                this.configElements = [];
                message.scopes.ConfigScope.forEach(element => {
                    if (!this.configElements.includes(element, 0)) {
                        this.configElements.push({
                            ID: element.name,
                            Time: element.date,
                            StackTrace: element.stackTrace,
                            Value: element.value
                        });
                    }
                });
                console.info(this.configElements);
            }
        }

        if (message.saveFiles) {
            console.info('Pulling save files...');
            this.savePoints = [];
            message.saveFiles.forEach(saveName => {
                this.savePoints.push(saveName);
                console.info(this.savePoints);
            });
        }

        if (message.simulator) {
            console.info('Pulling sim auth token...');
            this.simAuthToken = message.simulator.simAuthToken;
            this.simPort = message.simulator.simPort;
            this.simUrl = message.simulator.simUrl;
            this.simProtocol = message.simulator.simProtocol;
            if (message.simulator.simPort && message.simulator.simAuthToken && message.simulator.simAuthToken.length > 0) {
                this.simAuthTokenAvailable = true;
            } else {
                this.simAuthTokenAvailable = false;
            }
        } else {
            this.simAuthTokenAvailable = false;
        }
        if (message.customerDisplay) {
            console.info('Pulling customer display token...');
            this.customerDisplayAuthToken = message.customerDisplay.customerDisplayAuthToken;
            this.customerDisplayPort = message.customerDisplay.customerDisplayPort;
            this.customerDisplayUrl = message.customerDisplay.customerDisplayUrl;
            this.customerDisplayProtocol = message.customerDisplay.customerDisplayProtocol;
            this.customerDisplayPairedDeviceId = message.customerDisplay.pairedDeviceId;
            if (message.customerDisplay.customerDisplayPort &&
                message.customerDisplay.customerDisplayAuthToken &&
                message.customerDisplay.customerDisplayAuthToken.length > 0) {
                this.customerDisplayAuthTokenAvailable = true;
            } else {
                this.customerDisplayAuthTokenAvailable = false;
            }
        } else {
            this.customerDisplayAuthTokenAvailable = false;
        }
    }

    @HostListener('document:keydown', ['$event'])
    handleKeydownEvent(event: any) {
        const key = event.key;
        // console.info(key);
        if (key === 'ArrowUp' && this.keyCount !== 1) {
            this.keyCount = 1;
        } else if (key === 'ArrowUp' && this.keyCount === 1) {
            this.keyCount = 2;
        } else if (key === 'ArrowDown' && this.keyCount === 2) {
            this.keyCount = 3;
        } else if (key === 'ArrowDown' && this.keyCount === 3) {
            this.keyCount = 4;
        } else if (key === 'ArrowLeft' && this.keyCount === 4) {
            this.keyCount = 5;
        } else if (key === 'ArrowRight' && this.keyCount === 5) {
            this.keyCount = 6;
        } else if (key === 'ArrowLeft' && this.keyCount === 6) {
            this.keyCount = 7;
        } else if (key === 'ArrowRight' && this.keyCount === 7) {
            this.keyCount = 8;
        } else if ((key === 'b' || key === 'B') && this.keyCount === 8) {
            this.keyCount = 9;
        } else if ((key === 'a' || key === 'A') && this.keyCount === 9) {
            this.onDevMenuClick();
            this.keyCount = 0;
        } else {
            this.keyCount = 0;
        }
    }

    @HostListener('document:click', ['$event'])
    documentClick(event: any) {
        const screenWidth = window.innerWidth;
        const screenHeight = window.innerHeight;
        let x = event.clientX;
        let y = event.clientY;
        if (event.type === 'touchstart') {
            // console.info(event);
            x = event.changedTouches[0].pageX;
            y = event.changedTouches[0].pageY;
        }
        // console.info(`${screenWidth} ${x} ${y}`);
        if (this.clickCount === 0 || Date.now() - this.firstClickTime > 1000 ||
            (y > 100) || this.disableDevMenu) {
            this.firstClickTime = Date.now();
            this.clickCount = 0;
        }

        if (y < 100) {
            this.clickCount = ++this.clickCount;
        }

        if (y < 200 && x < 200) {
            this.devClicks = 1;
        } else if ((y < 200 && x > screenWidth - 200) && (this.devClicks === 1 || this.devClicks === 2)) {
            this.devClicks = 2;
        } else if ((y > screenHeight - 200 && x > screenWidth - 200) && (this.devClicks === 2 || this.devClicks === 3)) {
            this.devClicks = 3;
        } else if ((y > screenHeight - 200 && x < 200) && this.devClicks === 3) {
            this.onDevMenuClick();
            this.devClicks = 0;
        } else {
            this.devClicks = 0;
        }

        // console.info(this.devClicks + " y="+y + ",x="+x+",h="+screenHeight+",w="+screenWidth);

    }

    public onStackTraceClose() {
        this.displayStackTrace = false;
    }

    public onDevMenuClick(): void {
        if (!this.showDevMenu) {
            this.pluginService.getPlugin('openPOSCordovaLogPlugin').then(
                (plugin: IOldPlugin) => {
                    this.logPlugin = plugin;
                    if (this.logPlugin && this.logPlugin.impl) {
                        this.logsAvailable = true;
                        this.logPlugin.impl.listLogFiles('DESC',
                            (fileNames) => {
                                this.logFilenames = fileNames;
                            },
                            (error) => {
                                this.logFilenames = [];
                            }
                        );
                    } else {
                        this.logsAvailable = false;
                    }
                }
            ).catch(error => {
                this.logsAvailable = false;
            });
        }

        if (this.personalization.getPersonalizationSuccessful$().getValue()) {
            this.session.publish('DevTools::Get', DevMenuComponent.MSG_TYPE);
        } else {
            console.info(`DevTools can't fetch server status since device is not yet personalized.`);
        }

        this.showDevMenu = !this.showDevMenu;

        if (!this.personalization.getPersonalizationSuccessful$().getValue()) {
            // Due to a bug in the WKWebview, the below is needed on cordova to get the
            // DevMenu to show on the iPad when personalization has failed.  Without this code,
            // the DevMenu is invisible until the iPad is rotated. With this code, though, there
            // is a side affect that two expansion panels are shown (one with content, one without).
            // Sigh.  But I am leaving this in for now at least so that *a* DevMenu shows.
            this.cd.detectChanges();
        }

        this.brand = this.personalization.getPersonalizationProperties$().getValue()?.get('brandId') ?? '';
    }

    public onDevMenuRefresh() {
        console.info('refreshing tools... ');
        this.displayStackTrace = false;
        this.currentState = 'Updating State... ';
        this.currentStateClass = 'Updating State...';
        this.showUpdating = true;
        this.currentStateActions = [];
        this.deviceElements = [];
        this.conversationElements = [];
        this.sessionElements = [];
        this.configElements = [];
        this.flowElements = [];
        setTimeout(() => {
            this.session.publish('DevTools::Get', DevMenuComponent.MSG_TYPE);
            this.showUpdating = false;
        }, 500
        );
        this.onCreateSavePoint(null);

    }

    public onNodeRemove(element: Element) {
        this.removeNodeElement(element);
    }

    public onSessRemove(element: Element) {
        this.removeSessionElement(element);
    }

    public onConvRemove(element: Element) {
        this.removeConversationElement(element);
    }

    public onConfRemove(element: Element) {
        this.removeConfigElement(element);
    }

    protected onFlowRemove(element: Element) {
        this.removeFlowElement(element);
    }

    public onStackTrace(element: Element) {
        this.displayStackTrace = true;
        this.selected = '\'' + element.ID + '\'';
        this.stackTrace = element.StackTrace;
    }

    public onLoadSavePoint(savePoint: string) {
        if (this.savePoints.includes(savePoint)) {
            this.session.publish('DevTools::Load::' + savePoint, DevMenuComponent.MSG_TYPE);
            console.info('Loaded Save Point: \'' + savePoint + '\'');
        } else {
            console.info('Unable to load Save Point: \'' + savePoint + '\'');
        }
    }

    public onSimulateScan(value: string) {
        console.info('onSimulatedScan(' + value + ')');
        if (value) {
            this.session.publish('DevTools::Scan', DevMenuComponent.MSG_TYPE, value);
        }
    }

    public onCreateSavePoint(newSavePoint: string) {
        if (newSavePoint) {
            this.addSaveFile(newSavePoint);
        }
        if (!this.displaySavePoints && this.savePoints && this.savePoints.length > 0) {
            this.displaySavePoints = true;
        }
    }

    public onSavePointRemove(savePoint: string) {
        this.removeSaveFile(savePoint);
        if (this.savePoints && this.savePoints.length === 0) {
            this.displaySavePoints = false;
        }
    }

    public onDevRefreshView() {
        this.personalization.refreshApp();
    }

    public onPersonalize() {
        this.devMenuPanel.close();

        this.personalization.dePersonalize();
        this.session.unsubscribe();

        // this will just take us through the personalization steps already, so let it?
        this.onDevRefreshView();
    }

    public onDevClearLocalStorage() {
        this.personalization.clearStorage().subscribe({
            next: () => this.personalization.refreshApp(),
            error: (error) => console.error(error),
            complete: () => this.personalization.refreshApp()
        });
    }

    public onOpenSimulator() {
        const serverName = this.personalization.getServerName$().getValue();
        const serverPort = this.personalization.getServerPort$().getValue();
        const protocol = this.simProtocol ? this.simProtocol : window.location.protocol;
        const sslEnabled = this.simProtocol && this.simProtocol === 'https' ? 'true' : 'false';
        const displayPort = location.port === '4200' ? location.port : this.simPort;
        const url = this.simUrl ? this.simUrl : window.location.hostname;
        const sim = protocol + '://' + url + ':'
            + displayPort + '/#/?serverName=' + serverName + '&serverPort=' + serverPort
            + '&deviceToken=' + this.simAuthToken + '&sslEnabled=' + sslEnabled;
        window.open(sim);
    }

    public onOpenCustomerDisplay() {
        const serverName = this.personalization.getServerName$().getValue();
        const serverPort = this.personalization.getServerPort$().getValue();
        const protocol = this.customerDisplayProtocol ? this.customerDisplayProtocol : window.location.protocol;
        const displayPort = location.port === '4200' ? location.port : this.customerDisplayPort;
        const url = this.customerDisplayUrl ? this.customerDisplayUrl : window.location.hostname;
        const sslEnabled = this.customerDisplayProtocol && this.customerDisplayProtocol === 'https' ? 'true' : 'false';

        const customerDisplay = protocol + '://' + url + ':'
            + displayPort + '/#/?serverName=' + serverName + '&serverPort=' + serverPort
            + '&deviceToken=' + this.customerDisplayAuthToken + '&sslEnabled=' + sslEnabled
            + '&pairedDeviceId=' + this.customerDisplayPairedDeviceId;
        window.open(customerDisplay);
    }

    public onDevResetDevice(): void {
        this.session.publish('DevTools::Reset::Device', DevMenuComponent.MSG_TYPE);
        this.session.connectToStomp();
    }

    public onDevRestartNode(): Promise<{ success: boolean, message: string }> {
        const prom = new Promise<{ success: boolean, message: string }>((resolve, reject) => {
            const port = this.personalization.getServerPort$().getValue();
            const nodeId = this.personalization.getDeviceId$().getValue().toString();
            const url = `${this.discovery.getServerBaseURL()}/register/restart/node/${nodeId}`;
            const httpClient = this.httpClient;
            httpClient.get(url).subscribe(response => {
                const msg = `Node '${nodeId}' restarted successfully.`;
                console.info(msg);
                resolve({ success: true, message: msg });
            },
                err => {
                    const msg = `Node restart Error occurred: ${JSON.stringify(err)}`;
                    const statusCode = err.status || (err.error ? err.error.status : null);
                    let errMsg = '';
                    if (err.error) {
                        if (err.error.error) {
                            errMsg += err.error.error;
                        }
                        if (err.error.message) {
                            errMsg += (errMsg ? '; ' : '') + err.error.message;
                        }
                    }
                    const returnMsg = `${statusCode ? statusCode + ': ' : ''}` +
                        (errMsg ? errMsg : 'Restart failed. Check client and server logs.');
                    reject({ success: false, message: returnMsg });
                });

        });
        return prom;
    }

    public onChangeBrand(): void {
        this.session.publish('DevTools::Brand', DevMenuComponent.MSG_TYPE, { brand: this.brand });
        this.personalization.setPersonalizationProperties(
            this.personalization.getPersonalizationProperties$().getValue().set('brandId', this.brand)
        );
        this.session.connectToStomp();
        this.onDevMenuClick();
    }

    public onLogfileSelected(logFilename: string): void {
        this.currentSelectedLogfilename = logFilename;
    }

    public onLogfileShare(logFilename?: string): void {
        if (this.logPlugin && this.logPlugin.impl) {
            const targetFilename = logFilename || this.currentSelectedLogfilename;
            this.logPlugin.impl.shareLogFile(
                targetFilename,
                () => {
                },
                (error) => {
                    console.info(error);
                }
            );
        }
    }

    public onLogfileUpload(logFilename?: string): void {
        if (this.logPlugin && this.logPlugin.impl) {
            const targetFilename = logFilename || this.currentSelectedLogfilename;
            this.logPlugin.impl.getLogFilePath(
                targetFilename,
                (logfilePath) => {
                    this.fileUploadService.uploadLocalDeviceFileToServer('log', targetFilename, 'text/plain', logfilePath)
                        .then((result: { success: boolean, message: string }) => {
                            this.snackBar.open(result.message, 'Dismiss', {
                                duration: 8000, verticalPosition: 'bottom'
                            });
                        })
                        .catch((result: { success: boolean, message: string }) => {
                            this.snackBar.open(result.message, 'Dismiss', {
                                duration: 8000, verticalPosition: 'bottom'
                            });
                        });
                },
                (error) => {
                    console.info(error);
                }
            );
        }
    }

    public onLogfileView(logFilename?: string): void {
        if (this.logPlugin && this.logPlugin.impl) {
            const targetFilename = logFilename || this.currentSelectedLogfilename;
            this.logPlugin.impl.readLogFileContents(
                targetFilename,
                (logFileContents) => {
                    this.devMenuPanel.close();
                    const dialogRef = this.dialog.open(FileViewerComponent, {
                        panelClass: 'openpos-default-theme',
                        maxWidth: '100vw', maxHeight: '100vh', width: '100vw'
                    });
                    dialogRef.componentInstance.fileName = targetFilename;
                    dialogRef.componentInstance.text = logFileContents;
                },
                (error) => {
                    console.info(error);
                }
            );
        }
    }

    public versions(): IVersion[] {
        return this.configurationService.versions;
    }

    protected addSaveFile(newSavePoint: string) {
        if (newSavePoint) {
            if (!this.savePoints.includes(newSavePoint)) {
                this.savePoints.push(newSavePoint);
            }
            this.session.publish('DevTools::Save::' + newSavePoint, DevMenuComponent.MSG_TYPE);
            console.info('Save Point Created: \'' + newSavePoint + '\'');
        }
    }

    protected removeSaveFile(saveName: string) {
        console.info('Attempting to remove Save Point \'' + saveName + '\'...');
        const index = this.savePoints.findIndex(item => {
            return saveName === item;
        });
        if (index !== -1) {
            this.session.publish('DevTools::RemoveSave::' + saveName, DevMenuComponent.MSG_TYPE);
            this.savePoints.splice(index, 1);
            console.info('Save Points updated: ');
            console.info(this.savePoints);
        }
    }

    protected removeNodeElement(element: Element) {
        console.info('Attempting to remove \'' + element.Value + '\'...');
        const index = this.deviceElements.findIndex(item => {
            return element.Value === item.Value;
        });
        if (index !== -1) {
            this.session.publish('DevTools::Remove::Node', DevMenuComponent.MSG_TYPE, element);
            this.deviceElements.splice(index, 1);
            console.info('Node Scope updated: ');
            console.info(this.deviceElements);
        }
    }

    public useSimulatedScanner(): boolean {
        return CONFIGURATION.useSimulatedScanner;
    }

    public isElectronEnabled() {
        return this.electronPlatform.platformPresent();
    }

    public async toggleChromiumDevTools()  {
        if (this.isElectronEnabled()) {
            await window.openposElectron.toggleDevTools();
        }
    }

    public async exitElectronApp() {
        if (this.isElectronEnabled()) {
            await window.openposElectron.quit();
        }
    }

    public getLocalTheme(): Observable<string> {
        return this.configurationService.theme$;
    }

    public removeSessionElement(element: Element) {
        console.info('Attempting to remove \'' + element.Value + '\'...');
        const index = this.sessionElements.findIndex(item => {
            return element.Value === item.Value;
        });
        if (index !== -1) {
            this.session.publish('DevTools::Remove::Session', DevMenuComponent.MSG_TYPE, element);
            this.sessionElements.splice(index, 1);
            console.info('Session Scope updated: ');
            console.info(this.deviceElements);
        }
    }

    onEnableAutoPersonalizationChanged(event: MatSlideToggleChange) {
        console.log('personalization touched', event);
        this.personalization.setSkipAutoPersonalization(!event.checked);
        this.autoPersonalizationSettingTouched = true;
    }

    protected removeConversationElement(element: Element) {
        console.info('Attempting to remove \'' + element.Value + '\'...');
        const index = this.conversationElements.findIndex(item => {
            return element.Value === item.Value;
        });
        if (index !== -1) {
            this.session.publish('DevTools::Remove::Conversation', DevMenuComponent.MSG_TYPE, element);
            this.conversationElements.splice(index, 1);
            console.info('Conversation Scope updated: ');
            console.info(this.conversationElements);
        }
    }

    protected removeConfigElement(element: Element) {
        console.info('Attempting to remove \'' + element.Value + '\'...');
        const index = this.configElements.findIndex(item => {
            return element.Value === item.Value;
        });
        if (index !== -1) {
            this.session.publish('DevTools::Remove::Config', DevMenuComponent.MSG_TYPE, element);
            this.configElements.splice(index, 1);
            console.info('Config Scope updated: ');
            console.info(this.configElements);
        }
    }

    protected removeFlowElement(element: Element) {
        console.info('Attempting to remove \'' + element.Value + '\'...');
        const index = this.flowElements.findIndex(item => {
            return element.Value === item.Value;
        });
        if (index !== -1) {
            this.session.publish('DevTools::Remove::Flow', DevMenuComponent.MSG_TYPE, element);
            this.flowElements.splice(index, 1);
            console.info('Flow Scope updated: ');
            console.info(this.flowElements);
        }
    }
}
