import {
    Directive,
    ElementRef,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    Output,
    Renderer2,
    SimpleChanges
} from '@angular/core';
import { merge, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { IActionItem } from '../../core/actions/action-item.interface';
import { ActionService } from '../../core/actions/action.service';
import { KeybindingZoneService } from '../../core/keybindings/keybinding-zone.service';

@Directive({
    // tslint:disable-next-line:directive-selector
    selector: '[actionItem]'
})
export class ActionItemKeyMappingDirective implements OnChanges, OnDestroy {
    private actionItemChanged$ = new Subject();
    private destroyed$ = new Subject();

    @Output() actionClick = new EventEmitter();

    @Input()
    actionItem: IActionItem;

    @Input()
    actionItemPayload: any;

    constructor(
        private renderer: Renderer2,
        private el: ElementRef,
        private actionService: ActionService,
        private keybindingZoneService: KeybindingZoneService) {
    }

    updateKeybinding(): void {
        console.debug(`[ActionItemKeyMappingDirective]: Adding keybinding ${this.actionItem.keybind}`, this.actionItem);
        this.keybindingZoneService.addKeybinding(this.actionItem);

        this.keybindingZoneService.getNeedActionPayload(this.actionItem.keybind)
            .pipe(
                takeUntil(merge(this.actionItemChanged$, this.destroyed$)),
            ).subscribe(event => event.payload = this.actionItemPayload);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.actionItem) {
            console.debug(`[ActionItemKeyMappingDirective]: Removing keybinding ${changes.actionItem.previousValue}`);
            this.keybindingZoneService.removeKeybinding(changes.actionItem.previousValue);
            this.actionItemChanged$.next();

            if (changes.actionItem.currentValue) {
                this.updateKeybinding();
            }
        }
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
    }
}
