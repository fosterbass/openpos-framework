import { registerPlugin } from '@capacitor/core';

export const configProvider = registerPlugin<CapacitorConfigProvider>('ConfigProvider');

export interface CapacitorConfigProvider {
    getConfig(): Promise<any>;
}
