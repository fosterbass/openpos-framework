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
import { filter, takeUntil } from 'rxjs/operators';
import { IActionItem } from '../../core/actions/action-item.interface';
import { ActionService } from '../../core/actions/action.service';
import { KeybindingZoneService } from '../../core/keybindings/keybinding-zone.service';
import { KeybindingAction } from '../../core/keybindings/keybinding-action.interface';

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
        // If this keybinding exists it will be handled by the KeybindingService
        if (!this.keybindingZoneService.findActionByKey(this.actionItem.keybind)) {
            console.debug(`[ActionItemKeyMappingDirective]: Adding keybinding ${this.actionItem.keybind}`, this.actionItem);
            this.keybindingZoneService.addKeybinding(this.actionItem);
        }

        this.keybindingZoneService.getNeedActionPayload(this.actionItem.keybind)
            .pipe(
                filter(keybindingAction => this.isMyAction(keybindingAction)),
                takeUntil(merge(this.actionItemChanged$, this.destroyed$)),
            ).subscribe(keybindingAction => keybindingAction.payload = this.actionItemPayload);
    }

    isMyAction(keybindingAction: KeybindingAction): boolean {
        return this.actionItem && this.actionItem.action === keybindingAction.action.action;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.actionItem) {
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
