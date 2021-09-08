import { registerPlugin } from "@capacitor/core";

export const ConfigProvider = registerPlugin<CapacitorConfigProvider>('ConfigProvider');

export interface CapacitorConfigProvider {
    getConfig(): Promise<any>;
}
