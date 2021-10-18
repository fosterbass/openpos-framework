
import { Directive } from '@angular/core';

@Directive({
    // tslint:disable-next-line: directive-selector
    selector: 'input',
    // tslint:disable-next-line: no-host-metadata-property
    host: {
        autocomplete: 'off',
        autocorrect: 'off',
        autocapitalize: 'off',
        spellcheck: 'false'
    }
})
export class DisableAutoCompleteDirective {

    constructor() { }
}
