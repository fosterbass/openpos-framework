import { Injectable } from '@angular/core';
import { ConfigurableFocusTrap, ConfigurableFocusTrapFactory } from '@angular/cdk/a11y';

@Injectable({
    providedIn: 'root',
})
export class FocusService {

    private focusTrap: ConfigurableFocusTrap;

    constructor(private focusTrapFactory: ConfigurableFocusTrapFactory) { }

    destroy() {
        if (!!this.focusTrap) {
            this.focusTrap.destroy();
        }
    }

    createInitialFocus(element: HTMLElement): Promise<boolean> {
        this.focusTrap = this.focusTrapFactory.create(element);
        return this.focusTrap.focusInitialElementWhenReady();
    }

    restoreInitialFocus() {
        if (!!this.focusTrap) {
            this.focusTrap.focusInitialElement();
        }
    }

    restoreFocus(element: HTMLElement) {
        if (element) {
            element.focus();
        }
    }

    blurCurrentElement() {
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }
    }

}
