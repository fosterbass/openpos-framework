import { registerPlugin, PluginListenerHandle } from '@capacitor/core';
import { ScanDataType } from '../scanner';

export const scandit = registerPlugin<ScanditPlugin>('ScanditNative');

export type ScanditEvents = 'scan';

export interface ScanditPlugin {
    initialize(args: InitializeArguments): Promise<void>;
    addView(args?: AddViewArguments): Promise<void>;
    removeView(args?: RemoveViewArguments): Promise<void>;
    updateView(constraints: ScanditViewConstraints): Promise<void>;

    addListener(event: 'scan', callback: (e: ScanditScanData) => void): PluginListenerHandle;
}

export type ScanditSymbology =
    ScanDataType & (
        'CODABAR' |
        'CODE39' |
        'CODE93' |
        'CODE128' |
        'CODE11' |
        'EAN8' |
        'GS1DATABAR' |
        'MSI_PLESSEY' |
        'PDF417' |
        'MICROPDF417' |
        'DATAMATRIX' |
        'AZTEK' |
        'QRCODE' |
        'MAXICODE' |
        'UPCE' |
        'MATRIX_2OF5' |
        'UPCA' |
        'CODE25_I2OF5');

export interface ScanditScanData {
    symbology: ScanditSymbology;
    data: string;
}

export interface InitializeArguments {
    apiKey: string;
}

export interface AddViewArguments {
    [x: string]: any;
}

export interface RemoveViewArguments {
    [x: string]: any;
}

export interface ScanditViewConstraints {
    left: number;
    top: number;
    width: number;
    height: number;
}
