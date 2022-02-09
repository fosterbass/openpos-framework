import type { QueryList } from '@angular/core';
import { ContentChildren, Directive, OnDestroy, OnInit } from '@angular/core';
import { ArrowTabItemDirective } from './arrow-tab-item.directive';
import { Subject, Subscription } from 'rxjs';
import { filter, takeUntil, tap } from 'rxjs/operators';
import { KeybindingZoneService } from '../../core/keybindings/keybinding-zone.service';

@Directive({
    selector: '[appArrowTab]'
})
export class ArrowTabDirective implements OnInit, OnDestroy {
    @ContentChildren(ArrowTabItemDirective)
    buttons: QueryList<ArrowTabItemDirective>;
    destroyed$ = new Subject();

    private _subscription: Subscription;

    constructor(private keybindingZoneService: KeybindingZoneService) {
    }

    ngOnInit(): void {
        this.keybindingZoneService.getKeyDownEvent('ArrowUp,ArrowDown')
            .pipe(
                tap(event => console.debug('[ArrowTabDirective]: Received event', event)),
                filter(event => !event.domEvent.repeat),
                takeUntil(this.destroyed$)
            ).subscribe(event => {
                if (event.domEvent.key === 'ArrowUp') {
                    this.previous();
                } else if (event.domEvent.key === 'ArrowDown') {
                    this.next();
                }
            });
    }

    previous(): void {
        let index = this.buttons ? this.buttons.length : -1;
        const activeButton = this.buttons.toArray().filter(v =>
            v.nativeElement === document.activeElement || v.nativeElement.contains(document.activeElement));
        if (activeButton && activeButton.length > 0) {
            index = this.buttons.toArray().indexOf(activeButton[0]);
        }

        let newIndex = index - 1;
        while (newIndex >= 0 && this.buttons.toArray()[newIndex].isDisabled()) {
            newIndex--;
        }
        if ( newIndex >= 0) {
            this.buttons.toArray()[newIndex].nativeElement.focus();
        }
        console.debug('[ArrowTabDirective]: Selecting previous', this.buttons.toArray()[newIndex], this.buttons);
    }

    next(): void {
        let index = -1;
        const activeButton = this.buttons.toArray().filter(v =>
            v.nativeElement === document.activeElement || v.nativeElement.contains(document.activeElement));
        if (activeButton && activeButton.length > 0) {
            index = this.buttons.toArray().indexOf(activeButton[0]);
        }

        let newIndex = index + 1;
        while (newIndex < this.buttons.length && this.buttons.toArray()[newIndex].isDisabled()) {
            newIndex++;
        }
        if ( newIndex < this.buttons.length) {
            this.buttons.toArray()[newIndex].nativeElement.focus();
        }

        console.debug('[ArrowTabDirective]: Selecting next', this.buttons.toArray()[newIndex], this.buttons);
    }

    ngOnDestroy(): void {
        if (this._subscription) {
            this._subscription.unsubscribe();
        }
        this.destroyed$.next();
    }
}
