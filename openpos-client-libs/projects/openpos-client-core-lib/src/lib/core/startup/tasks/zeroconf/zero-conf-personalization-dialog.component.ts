
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, timer } from 'rxjs';
import { first, map, publish, publishBehavior, refCount } from 'rxjs/operators';

@Component({
    templateUrl: 'zero-conf-personalization-dialog.component.html',
    styleUrls: ['zero-conf-personalization-dialog.component.scss']
})

export class ZeroConfPersonalizationDialogComponent implements OnInit {
    connectionSteps = [
        'Service Discovery',
        'Configuring',
        'Connecting'
    ];

    canManuallyPersonalize$: Observable<boolean>;

    constructor(
        private _ref: MatDialogRef<ZeroConfPersonalizationDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public currentStep$: Observable<number>
    ) {
        // a delayed flag that shows the "Connect Manually" button once signaled
        // after 90 seconds
        this.canManuallyPersonalize$ = timer(90000).pipe(
            first(),
            map(() => true),
            publishBehavior(false),
            refCount()
        );
    }

    ngOnInit() { }

    cancel() {
        console.info('user requested manual personalization');
        this._ref.close(false);
    }
}
