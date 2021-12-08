import { ScreenPartComponent } from '../screen-part';
import { StatusStripInterface } from './status-strip.interface';
import { MatDialog } from '@angular/material/dialog';
import { Component, Injector, Input, OnDestroy } from '@angular/core';
import { ScreenPart } from '../../decorators/screen-part.decorator';
@ScreenPart({
    name: 'statusStrip'
})
@Component({
    selector: 'app-status-strip',
    templateUrl: './status-strip.component.html',
    styleUrls: ['./status-strip.component.scss'],
})
export class StatusStripComponent extends ScreenPartComponent<StatusStripInterface> implements OnDestroy {

    @Input()
    showTimestamps = true;

    date = Date.now();
    timer: number;
    dateInterval: NodeJS.Timeout;
    timerInterval: NodeJS.Timeout;

    constructor(protected dialog: MatDialog, injector: Injector) {
        super(injector);
    }

    screenDataUpdated() {
        this.dateInterval = setInterval(() => {
            this.date = Date.now();
        }, 1000);

        this.timerInterval = setInterval(() => {
            if (this.screenData.timestampBegin) {
                const timestampBegin = new Date(this.screenData.timestampBegin).getTime();
                this.timer = ((new Date()).getTime() - timestampBegin) / 1000;
            }
        }, 1000);
    }

    ngOnDestroy() {
        super.ngOnDestroy();
        if (this.dateInterval) {
            clearInterval(this.dateInterval);
        }
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
        }
    }

}
