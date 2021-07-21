import { Component, OnDestroy } from '@angular/core';
import { PosScreen } from '../pos-screen/pos-screen.component';
import { FormGroup, ValidatorFn, FormControl } from '@angular/forms';
import { ScreenComponent } from '../../shared/decorators/screen-component.decorator';
import { IItem } from '../../core/interfaces/item.interface';
import { IFormElement } from '../../core/interfaces/form-field.interface';
import { IActionItem } from '../../core/interfaces/action-item.interface';
import { ValidatorsService } from '../../core/services/validators.service';
import { ActionIntercepter, ActionIntercepterBehavior, ActionIntercepterBehaviorType } from '../../core/action-intercepter';
import { Observable } from 'rxjs';
import { OpenposMediaService } from '../../core/services/openpos-media.service';

/**
 * @ignore
 */
@ScreenComponent({
    name: 'Tendering'
})
@Component({
    selector: 'app-tendering',
    templateUrl: './tendering.component.html'
})
export class TenderingComponent extends PosScreen<any> implements OnDestroy {

    text: string;
    tenderItems: IItem[];
    tenderAmount: IFormElement;
    balanceDueAmount: IFormElement;
    balanceDue: string;
    totalAmount: IFormElement;
    itemActions: IActionItem[] = [];
    actionButton: IActionItem;
    isMobile: Observable<boolean>;

    tenderFormGroup: FormGroup;

    constructor(private validatorsService: ValidatorsService, private mediaService: OpenposMediaService) {
        super();
        this.isMobile = mediaService.mediaObservableFromMap(new Map([
            ['xs', true],
            ['sm', false],
            ['md', false],
            ['lg', false],
            ['xl', false]
        ]));
    }

    ngOnDestroy(): void {
        if (this.screen.template.localMenuItems) {
            this.screen.template.localMenuItems.forEach(element => {
                this.session.unregisterActionPayload(element.action);
                this.session.unregisterActionIntercepter(element.action);
            });
        }
    }

    buildScreen(): void {
        this.text = this.screen.text;
        this.tenderItems = this.screen.tenderItems;
        this.tenderAmount = this.screen.tenderAmount;
        this.balanceDue = this.screen.balanceDue;
        this.balanceDueAmount = this.screen.balanceDueAmount;
        this.totalAmount = this.screen.totalAmount;
        this.itemActions = this.screen.itemActions;
        this.actionButton = this.screen.actionButton;

        this.tenderFormGroup = this.buildTenderFormGroup();
        this.registerLocalMenuActions(this.getSubmitFormGroupFieldName());
    }

    protected getSubmitFormGroupFieldName(): string {
        return 'tenderAmtFld';
    }

    protected registerLocalMenuActions(formGroupFieldName: string): void {
        if (this.screen.template.localMenuItems) {
            this.screen.template.localMenuItems.forEach(element => {
                let actionValueFn = () => {};
                let interceptorPayloadFn = (payload) => {};
                if (this.tenderFormGroup.get(formGroupFieldName)) {
                    actionValueFn = () => this.tenderFormGroup.get(formGroupFieldName).value;
                    interceptorPayloadFn = (payload) => {
                        const value = this.tenderFormGroup.get(formGroupFieldName).value;
                        this.log.info(`Returning value of ${value}.  Payload: ${JSON.stringify(payload)}`);
                        return value;
                    }
                }
                this.session.registerActionPayload(element.action, actionValueFn);
                this.session.registerActionIntercepter(element.action,
                    new ActionIntercepter(this.log, interceptorPayloadFn,
                        // Will only block if the formGroup is inValid
                        new ActionIntercepterBehavior(ActionIntercepterBehaviorType.block,
                            tenderAmtValue => this.isTenderValid()
                        )
                    )
                );
            });
        }
    }

    protected buildTenderFormGroup(): FormGroup {
        const group: any = {};
        const validators: ValidatorFn[] = [];
        let formGroup: FormGroup = null;

        if (!!this.tenderAmount) {
            if (this.tenderAmount.validators) {
                this.tenderAmount.validators.forEach(v => {
                    const fn = this.validatorsService.getValidator(v);
                    if (fn) {
                        validators.push(fn);
                    }
                });
            }

            let disabled = false;
            if (this.tenderAmount.disabled) {
                disabled = this.tenderAmount.disabled;
            }
            group['tenderAmtFld'] = new FormControl({value: this.tenderAmount.value, disabled: disabled}, validators);
            formGroup = new FormGroup(group);
        } else {
            formGroup = new FormGroup({});
        }

        return formGroup;
    }

    onFormSubmit(): void {
        this.onAction();
    }

    isTenderValid(): boolean {
        const formControl = this.tenderFormGroup.get(this.getSubmitFormGroupFieldName());
        return this.tenderFormGroup.valid || formControl.disabled === true;
    }

    onAction(): void {
        const formControl = this.tenderFormGroup.get(this.getSubmitFormGroupFieldName());

        if (formControl) {
            if (this.isTenderValid()) {
                this.tenderAmount.value = formControl.value;
                this.session.onAction(this.actionButton.action, this.tenderAmount.value);
            }
        } else {
            this.session.onAction(this.actionButton.action);
        }
    }

}
