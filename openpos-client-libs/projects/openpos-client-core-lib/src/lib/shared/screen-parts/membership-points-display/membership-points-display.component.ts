import {Component, Input} from '@angular/core';
import {ScreenPartComponent} from '../screen-part';
import {MembershipPointsDisplayComponentInterface} from './membership-points-display.interface';
import {Observable} from "rxjs/internal/Observable";

@Component({
    selector: 'app-membership-points-display',
    templateUrl: './membership-points-display.component.html',
    styleUrls: ['./membership-points-display.component.scss']})
export class MembershipPointsDisplayComponent extends ScreenPartComponent<MembershipPointsDisplayComponentInterface>{

    @Input()
    isMobile: boolean;

    getMobileClass() : string { return this.isMobile ? 'mobile' : ''; }

    screenDataUpdated() {
    }
}
