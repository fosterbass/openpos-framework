import { Component, Inject } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { PeripheralSelectionService, PeripheralCategory, PeripheralDevice } from '../../../../core/peripherals/peripheral-selection.service';

export interface PeripheralSelectorConfirmationDialogData {
    device: PeripheralDevice;
}
@Component({
    templateUrl: './peripheral-selector-confirmation.component.html',
    styleUrls: ['./peripheral-selector-confirmation.component.scss']
})
export class PeripheralSelectorConfirmationComponent {
    shouldContinue = false;

    constructor(
        @Inject(MAT_DIALOG_DATA) public data: PeripheralSelectorConfirmationDialogData,
        private dialogRef: MatDialogRef<PeripheralSelectorConfirmationComponent>,
        public periph: PeripheralSelectionService
    ) { }

    continue() {
        this.shouldContinue = true;
        this.dialogRef.close();
    }

    close() {
        this.dialogRef.close();
    }
}
