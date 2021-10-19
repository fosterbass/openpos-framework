import {Injectable} from "@angular/core";
import {IPlatformPlugin} from "../platform-plugin.interface";
import {Observable} from "rxjs";
import {CordovaService} from "../../services/cordova.service";
import {filter, map} from "rxjs/operators";
import {MessageTypes} from "../../messages/message-types";
import {SessionService} from "../../services/session.service";
import {ActionMessage} from "../../messages/action-message";

export interface BluetoothDevice {
    name: string;
    address: string;
    id: string;
    class: number;
}

@Injectable({
    providedIn: 'root'
})
export class ZebraBluetoothPrinterCordovaPlugin implements IPlatformPlugin {
    printerMAC: string;
    printer: any;
    bluetoothSerial: any;
    printerAddress: string;
    printerName: string;
    bluetoothDevices: BluetoothDevice[];

    constructor(protected cordovaService: CordovaService, protected sessionService: SessionService) {

    }

    initialize(): Observable<string> {
        return new Observable<string>(observer => {
            return this.cordovaService.onDeviceReady.pipe(
                filter(m => m === 'deviceready'),
                map( () => {
                    // We depend on two plugins.  bluetoothSerial for getting a list of paired BT devices, and
                    // zbtprinter for printing to a Zebra printer
                    this.bluetoothSerial = window['bluetoothSerial'];
                    this.printer = window['cordova'].plugins['zbtprinter'];
                    this.bluetoothSerial.list(
                        (devices) => {
                            this.bluetoothDevices = devices;
                            console.info(`[ZebraBluetoothPrinterCordovaPlugin] found these paired devices: ${JSON.stringify(this.bluetoothDevices)}`);
                            observer.next(`ZebraBluetoothPrinterCordovaPlugin initialized`);
                            observer.complete();
                        },
                        (err) => {
                            console.error(`[ZebraBluetoothPrinterCordovaPlugin] failed to connect to Zebra printer over bluetooth: ${err}`);
                            // Just go ahead and complete so that startup is not failed completely.  Printing will
                            // fail at runtime.
                            observer.complete();
                        }
                    );

                    // Subscribe to Proxy messages that go to a printer
                    this.sessionService.getMessages('Proxy').pipe(
                        filter(m => m.proxyType === 'Print')
                    ).subscribe(message => {
                        this.bluetoothPrint(this.getPrinterAddress(message), message);
                    });
                })
            ).subscribe();
        });
    }

    /**
     * Uses the message.additionalFields.bluetoothDeviceName value to locate a BluetoothDevice entry
     * whose name matches that value.  If found, the address of that device will be used for printer communication.
     * @param message An OpenPOS ProxyMessage
     */
    getPrinterAddress(message: any) {
        if (!!message.additionalFields && !!message.additionalFields['bluetoothDeviceName']) {
            this.printerName = message.additionalFields['bluetoothDeviceName'];
            if (!!this.bluetoothDevices) {
                let device = this.bluetoothDevices.filter(d => this.printerName.toUpperCase() === d.name.toUpperCase());
                if (device.length > 0) {
                    this.printerAddress = device[0].address;
                } else {
                    console.warn(`[ZebraBluetoothPrinterCordovaPlugin] No bluetooth devices found with name: '${this.printerName}'`);
                    this.printerAddress = null;
                }
            } else {
                console.warn(`[ZebraBluetoothPrinterCordovaPlugin] List of bluetooth devices is unexpectedly empty'`);
                this.printerAddress = null;
            }
        } else {
            console.warn(`[ZebraBluetoothPrinterCordovaPlugin] No additionalField with name 'bluetoothDeviceName' found on print message.`);
            this.printerAddress = null;
        }

        return this.printerAddress;
    }

    /**
     * Sends the payload of the given message to the device at the given address.
     * @param printerAddress
     * @param message
     */
    bluetoothPrint(printerAddress: string, message: any) {
        if (!printerAddress) {
            console.warn(`[ZebraBluetoothPrinterCordovaPlugin] Printer address is empty, cannot print.`);
            this.handlePrintError(message, 'Printer address is empty, cannot print.');
            return;
        }

        if (message.action === 'Print') {
            console.info(`[ZebraBluetoothPrinterCordovaPlugin] Printing payload of size ${message.payload ? message.payload.length : '?'} to BT address '${printerAddress}'...`);
            this.printer.print(printerAddress, message.payload,
                successResponse => { this.handlePrintSuccess(message, successResponse); },
                failResponse => { this.handlePrintError(message, failResponse); }
            );
        }
    }

    handlePrintSuccess(message: any, response: string) {
        console.info(`[ZebraBluetoothPrinterCordovaPlugin] print successful`)

        const responseMessage = new ActionMessage('response', true,{ messageId: message.messageId, payload: response, success: true });
        responseMessage.type = MessageTypes.PROXY;
        this.sessionService.sendMessage(responseMessage);
    }

    handlePrintError(message: any, response: string) {
        console.error(`[ZebraBluetoothPrinterCordovaPlugin] print failed: ${response}`)
        const responseMessage = new ActionMessage('response', true, { messageId: message.messageId, payload: response, success: false });
        responseMessage.type = MessageTypes.PROXY;
        this.sessionService.sendMessage(responseMessage);
    }

    name(): string {
        return "ZebraBluetoothPrinterCordovaPlugin";
    }

    pluginPresent(): boolean {
        let present = false;
        if (window.hasOwnProperty('cordova')) {
            if (window['cordova'].hasOwnProperty('plugins')) {
                present = window['cordova'].plugins.hasOwnProperty('zbtprinter');
            }

            if (present) {
                console.debug(`zbtprinter is present`);
                present = window.hasOwnProperty('bluetoothSerial');
            }
        }
        console.debug(`zbtprinter and bluetoothSerial are present: ${present}`);
        return present;
    }
}