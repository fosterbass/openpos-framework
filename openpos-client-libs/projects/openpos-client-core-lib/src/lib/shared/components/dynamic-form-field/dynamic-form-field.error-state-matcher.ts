import {FormControl, FormGroupDirective, NgForm} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';

export class DynamicFieldErrorStateMatcher implements ErrorStateMatcher {

    error: string;

    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
        return (control && (control.dirty && control.invalid) || !!this.error);
    }
}
