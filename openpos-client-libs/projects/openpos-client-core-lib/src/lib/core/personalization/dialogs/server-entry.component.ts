import { Component, Inject, Optional } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup, Validators } from '@angular/forms';

export interface ServerEntryData {
    host?: string;
    port?: number;
    secure?: boolean;
    canCancel?: boolean;
}

@Component({
    templateUrl: './server-entry.component.html',
    styleUrls: ['./server-entry.component.scss']
})
export class ServerEntryComponent {
    readonly formGroup: FormGroup;

    constructor(
        private _dialogRef: MatDialogRef<ServerEntryComponent, ServerEntryData>,
        @Inject(MAT_DIALOG_DATA) @Optional() data: ServerEntryData
    ) {
        this.formGroup = new FormGroup({
            host: new FormControl(
                data?.host ?? window.location.hostname,
                Validators.required
            ),

            port: new FormControl(
                data?.port ?? window.location.port,
                [Validators.required, Validators.pattern(/^\d+$/),  Validators.min(1), Validators.max(65535)]
            ),

            secure: new FormControl(false)
        });
    }

    onSubmit() {
        this._dialogRef.close({
            host: this.formGroup.get('host').value,
            port: +this.formGroup.get('port').value,
            secure: !!this.formGroup.get('secure').value
        });
    }
}
