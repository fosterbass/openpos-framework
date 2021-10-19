import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { filter, map, mergeMap, take } from 'rxjs/operators';
import { MessageTypes } from '../../../core/messages/message-types';
import { SessionService } from '../../../core/services/session.service';
import { StatusDetailsComponent } from './status-details.component';
import { StatusDetailsService } from './status-details.service';

@Injectable({ providedIn: 'root' })
export class StatusDetailsDialog {
    private detailsDialog?: MatDialogRef<StatusDetailsComponent>;

    constructor(
        private session: SessionService,
        private dialog: MatDialog,
        public statusDetailsService: StatusDetailsService,
    ) {
        this.session.getMessages(MessageTypes.CLOSE_STATUS_DETAILS).subscribe(() => { this.closeDetails(); });
    }

    public openDetails() {
        if (this.detailsDialog) {
            return;
        }

        this.statusDetailsService.isDetailsNotEmpty().pipe(
            take(1),
            filter(isDetailsNotEmpty => isDetailsNotEmpty),
            map(() => {
                this.detailsDialog = this.dialog.open(StatusDetailsComponent, { minWidth: '75%' });
                return this.detailsDialog;
            }),
            mergeMap(dialog => dialog.afterClosed())
        ).subscribe({
            // don't need to worry about the subscription because the
            // observable will be automatically completed by the
            // source
            complete: () => {
                this.detailsDialog = undefined;
            }
        });
    }

    public closeDetails() {
        if (this.detailsDialog) {
            this.detailsDialog.close();
        }
    }
}
