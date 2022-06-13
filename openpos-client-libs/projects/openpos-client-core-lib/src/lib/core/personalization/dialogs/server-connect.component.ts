import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { ServerInitStatus, ServerStatusService } from '../server-status.service';

import { ServerEntryData } from './server-entry.component';

@Component({
    templateUrl: './server-connect.component.html',
    styleUrls: ['./server-connect.component.scss']
})
export class ServerConnectComponent implements OnInit, OnDestroy {
    serverInitStatus?: ServerInitStatus;

    private _connectSub?: Subscription;

    get canCancel(): boolean {
        return this.serverData.canCancel === undefined || this.serverData.canCancel;
    }

    constructor(
        private readonly _serverStatus: ServerStatusService,
        private readonly _dialogRef: MatDialogRef<ServerConnectComponent, boolean>,
        @Inject(MAT_DIALOG_DATA) public readonly serverData: ServerEntryData
    ) {}

    ngOnInit() {
        const protocol = !!this.serverData.secure ? 'https://' : 'http://';
        let portString = ':' + this.serverData.port.toString();

        if (!!this.serverData.secure && this.serverData.port === 443) {
            portString = '';
        } else if (!this.serverData.secure && this.serverData.port === 80) {
            portString = '';
        }

        this._connectSub = this._serverStatus.observeInitializationStatus(
            protocol + this.serverData.host + portString
        ).subscribe(data => {
            this.serverInitStatus = data;

            if (this.serverInitStatus?.isReady) {
                this._dialogRef.close(true);
            }
        });
    }

    ngOnDestroy(): void {
        this._connectSub?.unsubscribe();
        this._connectSub = undefined;
    }

    onCancel() {
        this._dialogRef.close(false);
    }
}
