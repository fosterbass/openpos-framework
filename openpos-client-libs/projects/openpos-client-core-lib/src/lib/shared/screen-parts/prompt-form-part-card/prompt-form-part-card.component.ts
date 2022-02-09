import { ScreenPartComponent } from '../screen-part';
import { AfterViewInit, Component, OnInit, ViewChild, Injector } from '@angular/core';
import { AbstractControl, Validators, ValidationErrors, FormControl, FormGroup, ValidatorFn } from '@angular/forms';
import { ValidatorsService } from '../../../core/services/validators.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { PromptFormPartCardInterface } from './prompt-form-part-card.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { merge } from 'rxjs';
import {ActionItem} from '../../../core/actions/action-item';
import { CustomDateValidator } from '../../validators/custom-date-validators';

@Component({
    selector: 'app-prompt-form-part-card',
    templateUrl: './prompt-form-part-card.component.html',
    styleUrls: ['./prompt-form-part-card.component.scss']
})
export class PromptFormPartCardComponent extends ScreenPartComponent<PromptFormPartCardInterface> implements AfterViewInit, OnInit {

    @ViewChild('optionsRef') options;

    stop$ = merge(this.beforeScreenDataUpdated$, this.destroyed$);
    promptFormGroup: FormGroup;
    promptDateFormGroup: FormGroup;
    initialized = false;
    instructions: string;
    previousInputLength: number;
    inputControlName = 'promptInputControl';
    hiddenInputControlName = 'promptInputHiddenDateControl';
    inputDateControlName = 'promptDateInputControl';
    hiddenDateInputControlName = 'promptDateInputHiddenDateControl';
    primaryActionButton: ActionItem;
    secondaryActionButton: ActionItem;
    public today: Date = new Date();

    get autoFocusPrompt(): boolean {
        // default to true if not properly defined... it is the way
        if (this.screenData.autoFocus === null || this.screenData.autoFocus === undefined) {
            return true;
        }

        return this.screenData.autoFocus;
    }

    constructor(private validatorsService: ValidatorsService, injector: Injector) {
        super(injector);
    }

    screenDataUpdated() {
        this.instructions = this.screenData.instructions;
        const group: any = {};
        const validators: ValidatorFn[] = [];
        if (this.screenData.isRequiredInputField !== false) {
            validators.push(Validators.required);
        }

        if (this.screenData.responseType) {
            validators.push(this.validatorsService.getValidator(this.screenData.responseType.toString()));
        }

        if (this.screenData.validators) {
            this.screenData.validators.forEach(v => validators.push(this.validatorsService.getValidator(v.toString())));
        }

        if (this.screenData.minLength !== null && this.screenData.minLength !== undefined) {
            validators.push(Validators.minLength(this.screenData.minLength));
        }

        if (this.screenData.maxLength !== null && this.screenData.maxLength !== undefined) {
            validators.push(Validators.maxLength(this.screenData.maxLength));
        }

        if (this.screenData.validationPatterns) {
            for (const validationPattern of this.screenData.validationPatterns) {
                validators.push(Validators.pattern(validationPattern));
            }
        }

        if (this.screenData.max !== null && this.screenData.max !== undefined) {
            validators.push(Validators.max(this.screenData.max));
        }

        if (this.screenData.min !== null && this.screenData.min !== undefined) {
            validators.push(Validators.min(this.screenData.min));
        }

        group[this.inputControlName] = new FormControl(this.screenData.responseText, validators);
        // When showing a DATE, there is also a hidden field to handle picking of dates using
        // a date picker, need to add a FormControl for that also.
        if (this.screenData.responseType && this.screenData.responseType.toString() !== 'DatePartChooser' &&
            this.screenData.responseType.toString().toLowerCase().indexOf('date') >= 0) {
            group[this.hiddenInputControlName] = new FormControl();
        }
        if (this.screenData.isExpiryDateEnabled) {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            group[this.inputDateControlName] = new FormControl('', [Validators.required, CustomDateValidator.minDate(yesterday.getTime())]);
            group[this.hiddenDateInputControlName] = new FormControl('', Validators.required);
        } else {
            group[this.inputDateControlName] = new FormControl();
            group[this.hiddenDateInputControlName] = new FormControl();
        }

        this.promptFormGroup = new FormGroup(group);
    }

    public keybindsEnabled() {
        return CONFIGURATION.enableKeybinds;
    }

    ngAfterViewInit(): void {
        this.initialized = true;
        this.primaryActionButton = this.screenData.actionButton;
        this.secondaryActionButton = this.screenData.secondaryActionButton;
    }

    onAction(menuItm: IActionItem) {
        this.doAction(menuItm);
    }

    onFormSubmit(): void {
        if (this.promptFormGroup.valid && this.options.nativeElement.children.length === 0) {
            const payload = this.promptFormGroup.value[this.inputControlName];
            const payload2 = this.promptFormGroup.value[this.inputDateControlName];
            if (this.screenData.actionButton) {
                this.doAction({ action: this.screenData.actionButton.action }, {creditCardNumber: payload, expiryDate: payload2});
            }
        }
    }

    onPromptInputChange(event): void {
       if (this.screenData.isGiftCardScanEnabled && !this.screenData.isExpiryDateEnabled) {
           if (event.target.value.length === 0) {
                    this.screenData.actionButton = this.primaryActionButton;
           } else {
                    this.screenData.actionButton = this.secondaryActionButton;
           }
       }
    }

}
