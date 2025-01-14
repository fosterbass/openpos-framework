import { Component, HostListener } from '@angular/core';
import { IConfirmationDialog } from '../../actions/confirmation-dialog.interface';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-confirmation-dialog',
    templateUrl: './confirmation-dialog.component.html',
    styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent {

    public confirmDialog: IConfirmationDialog;

    constructor(public dialogRef: MatDialogRef<ConfirmationDialogComponent>) {
    }

    @HostListener('document:keydown.escape', ['$event']) onEscape(event: KeyboardEvent) {
        this.deny();
    }

    confirm() {
        this.dialogRef.close(true);
    }

    deny() {
        this.dialogRef.close(false);
    }
}
