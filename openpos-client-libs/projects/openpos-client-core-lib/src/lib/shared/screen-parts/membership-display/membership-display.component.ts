import {Component, EventEmitter, Injector, Input, Output} from '@angular/core';
import {ScreenPartComponent} from '../screen-part';
import {Membership, MembershipDisplayComponentInterface} from './memebership-display.interface';

@Component({
    selector: 'app-membership-display',
    templateUrl: './membership-display.component.html',
    styleUrls: ['./membership-display.component.scss']
})
export class MembershipDisplayComponent extends ScreenPartComponent<MembershipDisplayComponentInterface> {
    @Input()
    membership: Membership;

    @Output()
    clickEvent: EventEmitter<Membership> = new EventEmitter();

    constructor(injector: Injector) {
        super(injector);
    }

    screenDataUpdated() {}
}
