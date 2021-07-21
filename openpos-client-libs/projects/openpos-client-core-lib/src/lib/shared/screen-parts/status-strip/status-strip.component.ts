import { ScreenPartComponent } from '../screen-part';
import { StatusStripInterface } from './status-strip.interface';
import { MatDialog } from '@angular/material';
import { timer } from 'rxjs';
import {Component, Injector, Input} from '@angular/core';
import { ScreenPart } from '../../decorators/screen-part.decorator';
@ScreenPart({
    name: 'statusStrip'
})
@Component({
    selector: 'app-status-strip',
    templateUrl: './status-strip.component.html',
    styleUrls: ['./status-strip.component.scss'],
})
export class StatusStripComponent extends ScreenPartComponent<StatusStripInterface> {

    date = Date.now();
    timer: number;

    @Input()
    showTimestamps: boolean = true;

    constructor(protected dialog: MatDialog, injector: Injector) {
        super(injector);
    }

    screenDataUpdated() {
        if(this.showTimestamps) {
            this.subscriptions.add(timer(1000, 1000).subscribe(() => {
                if (this.screenData.timestampBegin) {
                    const timestampBegin = this.screenData.timestampBegin;
                    this.timer = ((new Date(new Date().getTime() + new Date().getTimezoneOffset() * 60000)).getTime() - timestampBegin) / 1000;
                }
                this.date = Date.now();
            }));
        }
    }
}
