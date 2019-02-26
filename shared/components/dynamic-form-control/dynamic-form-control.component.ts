import { Component, ViewChildren, AfterViewInit, Input, QueryList, ViewChild } from '@angular/core';
import { FormGroup, AbstractControl } from '@angular/forms';
import { SessionService, ScreenService, IFormElement, IForm, FormBuilder, IActionItem } from '../../../core';
import { DynamicFormFieldComponent } from '../dynamic-form-field/dynamic-form-field.component';
import { ShowErrorsComponent } from '../show-errors/show-errors.component';

@Component({
  selector: 'app-dynamic-form-control',
  templateUrl: './dynamic-form-control.component.html',
  styleUrls: ['./dynamic-form-control.component.scss']
})
export class DynamicFormControlComponent implements AfterViewInit {

  @ViewChildren(DynamicFormFieldComponent) children: QueryList<DynamicFormFieldComponent>;
  @ViewChild('formErrors') formErrors: ShowErrorsComponent;
  form: FormGroup;

  buttons: IFormElement[];

  private _screenForm: IForm;
  private _alternateSubmitActions: string[];
  @Input() submitButton: IActionItem;

  constructor(public session: SessionService, public screenService: ScreenService, private formBuilder: FormBuilder) {}

  @Input()
  get screenForm(): IForm {
    return this._screenForm;
  }

  set screenForm( screenForm: IForm) {
    this._screenForm = screenForm;
    this.buttons = new Array<IFormElement>();

    this.form = this.formBuilder.group(screenForm);

    screenForm.formElements.forEach(element => {
        if (element.elementType === 'Button') {
        this.buttons.push(element);
        }
    });

  }

  @Input()
  get alternateSubmitActions(): string[] {
    return this._alternateSubmitActions;
  }

  set alternateSubmitActions( actions: string[] ) {
    this._alternateSubmitActions = actions;
    if (actions) {
      actions.forEach(action => {

        this.session.registerActionPayload(action, () => {
          if (this.form.valid) {
            this.formBuilder.buildFormPayload(this.form, this._screenForm);
            return this._screenForm;
          } else {
            // Show errors for each of the fields where necessary
            Object.keys(this.form.controls).forEach(f => {
              const control = this.form.get(f);
              control.markAsTouched({onlySelf: true});
            });
            throw Error('form is invalid');
          }
        });
      });
    }
  }


  ngAfterViewInit() {
    // Delays less than 1 sec do not work correctly.
    this.display(1000);
  }

  public display(delay: number) {
    const nonReadonlyChildren = this.children.filter( child => {
         return child.isReadOnly() === false;
      });

      if ( nonReadonlyChildren.length > 0 ) {
        setTimeout(() => nonReadonlyChildren[0].focus(), delay);
      }
  }

  submitForm() {
    if (this.form.valid) {
      this.formBuilder.buildFormPayload(this.form, this._screenForm);
      this.session.onAction(this.submitButton, this._screenForm);
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
  }

  onFieldChanged(formElement: IFormElement) {
    if (formElement.valueChangedAction) {
        this.formBuilder.buildFormPayload(this.form, this._screenForm);
      this.session.onAction(formElement.valueChangedAction, this._screenForm );
    }
  }

  onButtonClick(formElement: IFormElement) {
    this.session.onAction(formElement.buttonAction, null, formElement.confirmationDialog);
  }
}

