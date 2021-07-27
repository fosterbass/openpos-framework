import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Membership, MembershipDisplayComponentInterface} from './memebership-display.interface';
import {ScreenPartComponent} from '../screen-part';


@Component({
    selector: 'app-membership-display',
    templateUrl: './membership-display.component.html',
    styleUrls: ['./membership-display.component.scss']})
export class MembershipDisplayComponent extends ScreenPartComponent<MembershipDisplayComponentInterface> {
    @Input()
    membership: Membership;
    @Input()
    iconMatClass: string = 'mat-24';
    @Output()
    clickEvent: EventEmitter<Membership> = new EventEmitter();

    screenDataUpdated() {
    }
}
