import { Injectable } from '@angular/core';
import { StatusBar } from '@capacitor/status-bar';
import { CapacitorService } from '../../services/capacitor.service';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class CapacitorHideStatusbarStartupTask implements StartupTask {
    name = 'CapacitorHideStatusbarStartupTask';

    constructor(private _capacitor: CapacitorService) { }

    async execute(): Promise<void> {
        if (!this._capacitor.isRunningInCapacitor()) {
            throw new Error('not running within capacitor');
        }

        if (!this._capacitor.isPluginAvailable('StatusBar')) {
            throw new Error('StatusBar capacitor plugin not available');
        }

        await StatusBar.hide();
    }
}
