import { Directive, ElementRef, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { KeyPressProvider } from '../providers/keypress.provider';
import { fromEvent, Subject } from 'rxjs';
import { DisabledKeyPressProvider } from '../providers/disabled-keypress.provider';

@Directive({
    selector: '[appKeypressSource]',
    providers: [{ provide: KeyPressProvider, useClass: DisabledKeyPressProvider }],
})
export class KeyPressSourceDirective implements OnInit, OnDestroy {
    keydown$ = new Subject<KeyboardEvent>();
    keyup$ = new Subject<KeyboardEvent>();
    destroyed$ = new Subject();

    constructor(private renderer: Renderer2, private el: ElementRef, private keyPressProvider: KeyPressProvider) {
    }

    ngOnInit(): void {
        this.keyPressProvider.registerKeyPressSource(fromEvent<KeyboardEvent>(this.el.nativeElement, 'keyup'));
        this.keyPressProvider.registerKeyPressSource(fromEvent<KeyboardEvent>(this.el.nativeElement, 'keydown'));

        // Need to do this so that this element can grab key events
        this.renderer.setAttribute(this.el.nativeElement, 'tabindex', '0');
    }


    ngOnDestroy(): void {
        this.keyPressProvider.unregisterKeyPressSource(this.keyup$);
        this.keyPressProvider.unregisterKeyPressSource(this.keydown$);
        this.destroyed$.next();
    }
}
