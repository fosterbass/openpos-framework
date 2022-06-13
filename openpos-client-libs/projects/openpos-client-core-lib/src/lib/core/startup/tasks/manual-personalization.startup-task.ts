import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PersonalizationService } from '../../personalization/personalization.service';
import { ServerEntryComponent, ServerEntryData } from '../../personalization/dialogs/server-entry.component';
import { StartupTask } from '../startup-task';
import { PersonalizationEntryComponent } from '../../personalization/dialogs/personalization-entry.component';
import { BusinessUnitDevice } from '../../personalization/personalization-config';
import { openServerConnectingDialog } from './personalize-utils';

@Injectable({
    providedIn: 'root'
})
export class ManualPersonalizeStartupTask implements StartupTask {
    static readonly taskName = 'ManualPersonalizeStartupTask';

    constructor(
        private _dialog: MatDialog,
        private _personalization: PersonalizationService
    ) { }

    async execute(): Promise<any> {
        console.log('prompting for manual personalization');

        let serverEntryData: ServerEntryData;

        // Show the entry dialog followed by the connecting dialog until there's
        // actually a connection established.
        //
        // Interface wise, the connecting dialog will only close if the user
        // presses the cancel button.
        let isConnected = false;

        while (!isConnected) {
            serverEntryData = await this.openServerEntryDialog();
            isConnected = await openServerConnectingDialog(this._dialog, serverEntryData);
        }

        const device = await this.openPersonalizationDialog(serverEntryData);

        let params: Map<string, string>;
        if (!params) {
            params = new Map<string, string>(
                Object.keys(device.personalizationParamValues)
                    .map((key) => [key, device.personalizationParamValues[key]])
            );
        } else {
            params = new Map<string, string>();
        }

        await this._personalization.personalize({
            serverConnection: { host: serverEntryData.host, port: serverEntryData.port, secured: serverEntryData.secure, },
            businessUnitId: device.businessUnitId,
            deviceId: device.deviceId,
            appId: device.appId,
            authToken: device.authToken,
            params
        }).toPromise();
    }

    async openServerEntryDialog(): Promise<ServerEntryData> {
        const serverEntryDialog = this._dialog.open(
            ServerEntryComponent,
            {
                disableClose: true,
                hasBackdrop: false,
                panelClass: 'openpos-default-theme',
                width: '65vw',
                minWidth: '200px',
                maxWidth: '600px'
            }
        );

        return await serverEntryDialog.afterClosed().toPromise() as ServerEntryData;
    }

    async openPersonalizationDialog(entryFormData: ServerEntryData): Promise<BusinessUnitDevice> {
        const dialog = this._dialog.open(
            PersonalizationEntryComponent,
            {
                disableClose: true,
                hasBackdrop: false,
                panelClass: 'openpos-default-theme',
                width: '65vw',
                minWidth: '200px',
                maxWidth: '600px',
                data: entryFormData
            }
        );

        return await dialog.afterClosed().toPromise();
    }
}
