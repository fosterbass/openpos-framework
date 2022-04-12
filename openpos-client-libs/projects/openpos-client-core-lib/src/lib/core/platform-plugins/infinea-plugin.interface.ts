import { PluginListenerHandle } from '@capacitor/core';

export interface InfineaPlugin {
    addListener(event: any, callback: (e) => void): PluginListenerHandle;
    initialize(args?: InitializeArguments): Promise<void>;
    print(args: PrintArguments): Promise<void>;
}

export interface InitializeArguments {
    apiKey: string;
}

export interface PrintArguments {
    data: string;
}
