import { Component, EventEmitter, Input, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatBottomSheet, MatBottomSheetRef } from '@angular/material/bottom-sheet';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-bottom-sheet',
    templateUrl: './bottom-sheet.component.html',
    styleUrls: ['./bottom-sheet.component.scss']
})
export class BottomSheetComponent {
    @Input() label = '';
    @ViewChild('content') items: TemplateRef<any>;
    subscriptions: Subscription = new Subscription();
    @Output()
    sheetOpened: EventEmitter<MatBottomSheetRef> = new EventEmitter(undefined);

    ref: MatBottomSheetRef;

    constructor(private bottomSheet: MatBottomSheet) { }

    openSheet() {
        this.ref = this.bottomSheet.open(this.items, { panelClass: 'sheet' });
        this.sheetOpened.emit(this.ref);
    }

    closeSheet() {
        this.ref.dismiss();
    }
}
