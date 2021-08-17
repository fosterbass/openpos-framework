
declare module '@capacitor/core' {
    interface PluginRegistry {
        ConfigProvider: CapacitorConfigProvider;
    }
}

export interface CapacitorConfigProvider {
    getConfig(): Promise<any>;
}
