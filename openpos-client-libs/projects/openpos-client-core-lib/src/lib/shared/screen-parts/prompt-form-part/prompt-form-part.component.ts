import { ScreenPartComponent } from '../screen-part';
import { AfterViewInit, Component, OnInit, ViewChild, Injector, ChangeDetectorRef } from '@angular/core';
import { Validators, FormControl, FormGroup, ValidatorFn } from '@angular/forms';
import { ValidatorsService } from '../../../core/services/validators.service';
import { IActionItem } from '../../../core/actions/action-item.interface';
import { PromptFormPartInterface } from './prompt-form-part.interface';
import { CONFIGURATION } from '../../../configuration/configuration';
import { merge } from 'rxjs';
import {ActionItem} from '../../../core/actions/action-item';
import { KeybindingZoneService } from '../../../core/keybindings/keybinding-zone.service';

@Component({
    selector: 'app-prompt-form-part',
    templateUrl: './prompt-form-part.component.html',
    styleUrls: ['./prompt-form-part.component.scss']
})
export class PromptFormPartComponent extends ScreenPartComponent<PromptFormPartInterface> implements AfterViewInit, OnInit {

    @ViewChild('optionsRef') options;

    stop$ = merge(this.beforeScreenDataUpdated$, this.destroyed$);
    promptFormGroup: FormGroup;
    initialized = false;
    instructions: string;
    previousInputLength: number;
    inputControlName = 'promptInputControl';
    hiddenInputControlName = 'promptInputHiddenDateControl';
    primaryActionButton: ActionItem;
    secondaryActionButton: ActionItem;

    get autoFocusPrompt(): boolean {
        // default to true if not properly defined... it is the way
        if (this.screenData.autoFocus === null || this.screenData.autoFocus === undefined) {
            return true;
        }

        return this.screenData.autoFocus;
    }

    constructor(private validatorsService: ValidatorsService,
                injector: Injector,
                private changeDetectorRef: ChangeDetectorRef,
                private keybindingZoneService: KeybindingZoneService) {
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
        this.promptFormGroup = new FormGroup(group);
        // Let the default browser form submit handle this
        this.keybindingZoneService.removeKeybinding('Enter');
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
            if (this.screenData.actionButton) {
                this.doAction({ action: this.screenData.actionButton.action }, payload);
            }
        }
    }

    onPromptInputChange(event): void {
        if (this.screenData.isGiftCardScanEnabled) {
            if (event.target.value.length === 0) {
                this.screenData.actionButton = this.primaryActionButton;

            } else {
                this.screenData.actionButton = this.secondaryActionButton;

            }
            this.changeDetectorRef.detectChanges();
        }
    }

}
