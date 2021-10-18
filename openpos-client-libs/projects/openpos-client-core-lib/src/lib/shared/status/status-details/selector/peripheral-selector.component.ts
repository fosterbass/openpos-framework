import { Component, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import {
    PeripheralSelectionService,
    PeripheralCategory,
    PeripheralDevice
} from '../../../../core/peripherals/peripheral-selection.service';
import { map, take } from 'rxjs/operators';
import { PeripheralSelectorConfirmationComponent } from '../confirmation/peripheral-selector-confirmation.component';

export interface PeripheralSelectorDialogData {
    category: PeripheralCategory;
}
@Component({
    templateUrl: './peripheral-selector.component.html',
    styleUrls: ['./peripheral-selector.component.scss']
})
export class PeripheralSelectorComponent {
    private _openConfirmationDialog?: MatDialogRef<PeripheralSelectorConfirmationComponent>;

    constructor(
        @Inject(MAT_DIALOG_DATA) public data: PeripheralSelectorDialogData,
        private dialogRef: MatDialogRef<PeripheralSelectorComponent>,
        public periph: PeripheralSelectionService,
        private dialog: MatDialog,
    ) {
    }

    assignDevice(device: PeripheralDevice) {
        if (device.requiresConfirmation) {
            this._openConfirmationDialog = this.dialog.open(PeripheralSelectorConfirmationComponent, {
                data: { device },
                width: '50%'
            });

            this._openConfirmationDialog.afterClosed().pipe(take(1)).subscribe(() => {
                if (this._openConfirmationDialog.componentInstance.shouldContinue) {
                    this.periph.selectDevice(this.data.category, device);
                    this.dialogRef.close();
                }
                this._openConfirmationDialog = undefined;
            });
        } else {
            this.periph.selectDevice(this.data.category, device);
            this.dialogRef.close();
        }
    }

    close() {
        if (this._openConfirmationDialog) {
            this._openConfirmationDialog.close();
        }
        this.dialogRef.close();
    }
}
