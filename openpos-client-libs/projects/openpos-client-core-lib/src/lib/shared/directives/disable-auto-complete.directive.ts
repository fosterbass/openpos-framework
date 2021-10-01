
import { Directive } from '@angular/core';

@Directive({
    selector: 'input',
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
