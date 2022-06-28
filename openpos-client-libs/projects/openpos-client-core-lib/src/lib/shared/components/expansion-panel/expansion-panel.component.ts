import { Component, Input, OnChanges } from '@angular/core';
import { animate, state, style, transition, trigger } from '@angular/animations';

@Component({
    selector: 'app-expansion-panel',
    templateUrl: './expansion-panel.component.html',
    styleUrls: ['./expansion-panel.component.scss'],
    animations: [
        trigger('animationShowHide', [
            state('close', style({ height: '0px' })),
            state('open', style({ height: '*' })),
            transition('open <=> close', animate('250ms ease-in-out')),
        ]),
        trigger('animationRotate', [
            state('close', style({ transform: 'rotate(0)' })),
            state('open', style({ transform: 'rotate(-180deg)' })),
            transition('open <=> close', animate('250ms ease-in-out')),
        ]),
    ],
})
export class ExpansionPanelComponent implements OnChanges {

    @Input()
    expanded = true;

    state = 'open';

    constructor() {
    }

    ngOnChanges(): void {
        if (!this.expanded) {
            this.state = 'close';
        }
    }

    toggleExpanded() {
        this.state = this.state === 'close' ? 'open' : 'close';
    }

    isExpanded() {
        return this.state === 'open';
    }
}
