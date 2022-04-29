import type * as mdns from 'mdns';
declare global {
    interface Window {
        openposElectron?: ElectronGlobalContext;
    }

    interface ElectronGlobalContext {
        os: ElectronOS;
        zeroconf?: ZeroconfContext;
        toggleDevTools(): Promise<void>;
        quit(): Promise<void>;
    }

    interface ElectronOS {
        platform: string;
        arch: string;
        hostname: string;
    }

    interface ZeroconfContext {
        addListener(
            event: 'service-up' | 'service-down',
            type: string,
            domain: string,
            handler: (service: mdns.Service) => void
        ): () => void;
    }
}
