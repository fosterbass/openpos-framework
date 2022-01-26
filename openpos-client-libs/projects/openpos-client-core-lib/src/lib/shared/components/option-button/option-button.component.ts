import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnDestroy, Output, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { DomEventManager } from '../../../core/services/dom-event-manager.service';
import { CONFIGURATION } from '../../../configuration/configuration';

@Component({
    selector: 'app-option-button',
    templateUrl: './option-button.component.html',
    styleUrls: ['./option-button.component.scss']
})
export class OptionButtonComponent implements AfterViewInit, OnDestroy {
    @Input() disabled = false;
    @Input() inputType: string;
    @Input() optionTitle: string;
    @Input() optionIcon: string;
    @Input() optionSize: string;
    @Input() keybind: string;
    @Input() additionalText: string;
    @Input() additionalIcon: string;
    @Output() buttonClick = new EventEmitter();

    @ViewChild('button', {static: true}) button;

    private subscription: Subscription;

    constructor(private elementRef: ElementRef, private domEventManager: DomEventManager) {
    }

    ngAfterViewInit(): void {
        this.subscription = this.domEventManager.createEventObserver(this.elementRef.nativeElement, 'focus').subscribe(() => {
            this.button.focus();
        });
    }

    clickFn() {
        this.buttonClick.emit(true);
    }

    public keybindsEnabled(): boolean {
        return CONFIGURATION.enableKeybinds && !!this.keybind;
    }

    ngOnDestroy(): void {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }
}
