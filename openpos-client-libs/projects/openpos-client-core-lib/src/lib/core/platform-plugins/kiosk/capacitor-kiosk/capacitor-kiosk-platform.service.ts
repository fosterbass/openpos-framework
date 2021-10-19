import { Injectable } from '@angular/core';
import { Capacitor, registerPlugin } from '@capacitor/core';
import { KioskModePlatform } from '../kiosk-mode-platform';

const kioskMode = registerPlugin<CapKioskMode>('KioskMode');

interface CapKioskMode {
    isInKioskMode(): Promise<{ enabled: boolean }>;
    enter(): Promise<{ success: boolean }>;
    exit(): Promise<{ success: boolean }>;
}

@Injectable()
export class CapacitorKioskModePlatform implements KioskModePlatform {
    name(): string { return 'capacitor'; }

    isAvailable(): Promise<boolean> {
        return Promise.resolve(Capacitor.isPluginAvailable('KioskMode'));
    }

    async isInKioskMode(): Promise<boolean> {
        await this._verifyCapacitorPluginIsLoaded();

        return (await kioskMode.isInKioskMode()).enabled;
    }

    async enter(): Promise<void> {
        await this._verifyCapacitorPluginIsLoaded();

        if (!(await kioskMode.enter()).success) {
            throw Error('failed to enter kiosk mode');
        }
    }

    async exit(): Promise<void> {
        await this._verifyCapacitorPluginIsLoaded();

        if (!(await kioskMode.exit()).success) {
            throw Error('failed to exit kiosk mode');
        }
    }

    private async _verifyCapacitorPluginIsLoaded(): Promise<void> {
        if (!await this.isAvailable()) {
            throw new Error('capacitor plugin is not available');
        }
    }
}
