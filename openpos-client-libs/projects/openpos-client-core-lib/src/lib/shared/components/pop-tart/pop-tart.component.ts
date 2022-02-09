import { Component, Inject, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';

@Component({
    selector: 'app-pop-tart',
    templateUrl: './pop-tart.component.html',
    styleUrls: ['./pop-tart.component.scss'],
    providers: [KeybindingZoneService]
})
export class PopTartComponent implements OnDestroy {
    items: Array<string>;
    instructions: string;
    searchable: boolean;
    filterValue: string;

    constructor(@Inject(MAT_DIALOG_DATA) public data: any,
                public dialogRef: MatDialogRef<PopTartComponent>,
                public keybindingZoneService: KeybindingZoneService) {
        this.items = data.optionItems;
        this.instructions = data.instructions;
        this.searchable = data.searchable;
        this.keybindingZoneService.register('pop-tart');
        this.keybindingZoneService.activate();
    }

    ngOnDestroy(): void {
        this.unregisterKeybindings();
    }

    public select(item: string) {
        this.unregisterKeybindings();
        this.dialogRef.close(item);
    }

    public unregisterKeybindings(): void {
        this.keybindingZoneService.restorePreviousActivation();
        this.keybindingZoneService.unregister();
    }
}
