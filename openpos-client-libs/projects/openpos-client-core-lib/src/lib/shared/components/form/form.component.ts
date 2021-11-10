import {
    AfterViewInit,
    Component,
    ContentChild,
    ContentChildren,
    EventEmitter,
    Input,
    Output,
    QueryList
} from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';
import { ShowErrorsComponent } from '../show-errors/show-errors.component';
import { DynamicFormFieldComponent } from '../dynamic-form-field/dynamic-form-field.component';
import { FormBuilder } from '../../../core/services/form-builder.service';

/**
 * This is a component that wraps the form element so we can handle forms
 * in a consistent way througout the app.
 */
@Component({
    selector: 'app-form',
    templateUrl: './form.component.html',
    styleUrls: ['./form.component.scss']
})
export class FormComponent implements AfterViewInit {
    @ContentChildren(DynamicFormFieldComponent, {descendants: true}) children: QueryList<DynamicFormFieldComponent>;
    @ContentChild('formErrors') formErrors: ShowErrorsComponent;

    @Input()
    form: FormGroup;

    @Input()
    validateOnFirstLoad: boolean;

    /**
     * Submit event only emits if the form is valid
     */
    @Output()
    submitFormEvent = new EventEmitter();

    constructor(private formBuilder: FormBuilder) {
    }

    submitForm() {
        const shouldSubmit = this.form.valid && !this.form.pending;

        if (shouldSubmit) {
            this.submitFormEvent.emit();
        } else {
            // Set focus on the first invalid field found
            const invalidFieldKey = Object.keys(this.form.controls).find(key => {
                const ctrl: AbstractControl = this.form.get(key);
                return ctrl.invalid && ctrl.dirty;
            });
            if (invalidFieldKey) {
                const invalidField = this.children.find(f => f.controlName === invalidFieldKey).field;
                if (invalidField) {
                    const invalidElement = document.getElementById(invalidFieldKey);
                    if (invalidElement) {
                        invalidElement.scrollIntoView();
                    } else {
                        invalidField.focus();
                    }
                }
            } else {
                if (this.formErrors.shouldShowErrors()) {
                    const formErrorList = this.formErrors.listOfErrors();
                    if (formErrorList && formErrorList.length > 0) {
                        document.getElementById('formErrorsWrapper').scrollIntoView();
                    }
                }
            }
        }

        return shouldSubmit;
    }

    ngAfterViewInit(): void {
        if (this.validateOnFirstLoad) {
            // A setTimeout is required here. Forcing Angular to detect changes won't work here because
            // we have to wait for the current change detection cycle to finish so that all values
            // are set before validating the form.
            //
            // Without this, we get the dreaded Angular error: ExpressionChangedAfterItHasBeenCheckedError
            setTimeout(() => {
                this.formBuilder.markAllAsDirty(this.form);
                this.form.updateValueAndValidity();
            });
        }
    }
}
