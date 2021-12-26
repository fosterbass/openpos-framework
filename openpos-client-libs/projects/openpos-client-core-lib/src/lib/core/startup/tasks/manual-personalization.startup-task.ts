import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';
import { PersonalizationComponent } from '../../personalization/personalization.component';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class ManualPersonalizeStartupTask implements StartupTask {
    constructor(
        private _dialog: MatDialog
    ) { }

    execute(): Observable<any> {
        console.log('prompting for manual personalization');
        const dialogRef = this._dialog.open(
            PersonalizationComponent,
            {
                disableClose: true,
                hasBackdrop: false,
                panelClass: 'openpos-default-theme'
            }
        );

        return dialogRef.afterClosed().pipe(
            take(1)
        );
    }
}
