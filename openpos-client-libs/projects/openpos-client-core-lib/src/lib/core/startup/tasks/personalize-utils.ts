import { MatDialog } from '@angular/material/dialog';
import { ServerConnectComponent } from '../../personalization/dialogs/server-connect.component';
import { ServerEntryData } from '../../personalization/dialogs/server-entry.component';

export async function openServerConnectingDialog(dialog: MatDialog, entryFormData: ServerEntryData): Promise<boolean> {
    const serverConnectDialog = dialog.open(
        ServerConnectComponent,
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

    const didConnect = await serverConnectDialog.afterClosed().toPromise();
    return typeof didConnect === 'boolean' && didConnect;
}
