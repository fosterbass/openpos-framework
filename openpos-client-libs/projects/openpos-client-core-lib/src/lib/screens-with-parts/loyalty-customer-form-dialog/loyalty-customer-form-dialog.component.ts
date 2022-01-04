import {Component, Injector, QueryList, ViewChild, ViewChildren} from '@angular/core';
import { DialogComponent } from '../../shared/decorators/dialog-component.decorator';
import { PosScreen } from '../pos-screen/pos-screen.component';
import {LoyaltyCustomerFormInterface} from "./loyalty-customer-form.interface";
import {Observable} from "rxjs/internal/Observable";
import {MediaBreakpoints, OpenposMediaService} from "../../core/media/openpos-media.service";
import {IFormElement} from "../../core/interfaces/form-field.interface";
import {FormBuilder} from "../../core/services/form-builder.service";
import {ShowErrorsComponent} from "../../shared/components/show-errors/show-errors.component";
import { ScannerService } from '../../core/platform-plugins/scanners/scanner.service';
import {WedgeScannerPlugin} from "../../core/platform-plugins/scanners/wedge-scanner/wedge-scanner.plugin";


@DialogComponent({
    name: 'LoyaltyCustomerDialog',
})
@Component({
    selector: 'app-loyalty-customer-form-dialog',
    templateUrl: './loyalty-customer-form-dialog.component.html',
    styleUrls: [ './loyalty-customer-form-dialog.component.scss']
})
export class LoyaltyCustomerFormDialogComponent extends PosScreen<LoyaltyCustomerFormInterface> {

    isMobile: Observable<boolean>;
    @ViewChild('formErrors') formErrors: ShowErrorsComponent;

    firstNameField : any;
    lastNameField : IFormElement;
    loyaltyNumberField : IFormElement;
    extensionAttribute1Field : IFormElement;
    phoneField : IFormElement;
    phoneFields : IFormElement[] = [];
    phoneLabelFields : IFormElement[] = [];
    emailField : IFormElement;
    emailFields : IFormElement[] = [];
    emailLabelFields : IFormElement[] = [];

    handledFormFields : string[] = [];

    line1Field : IFormElement;
    line2Field : IFormElement;
    cityField : IFormElement;
    stateField : IFormElement;
    postalCodeField : IFormElement;
    countryField : IFormElement;
    addressIconLocationClass : string;

    scanStartControlCharacter : string;

    constructor(injector: Injector, private media: OpenposMediaService, private formBuilder: FormBuilder,
                private scannerService: ScannerService) {
        super(injector);
        this.initIsMobile();
    }

    ngOnInit() {
        const wedgeScannerPlugin = this.scannerService.getScanners().find(
                        scanner => scanner instanceof WedgeScannerPlugin);
        if ( wedgeScannerPlugin  ) {
            this.scanStartControlCharacter = (wedgeScannerPlugin as WedgeScannerPlugin).getStartSequence();
        }
    }

    initIsMobile(): void {
        this.isMobile = this.media.observe(new Map([
            [MediaBreakpoints.MOBILE_PORTRAIT, true],
            [MediaBreakpoints.MOBILE_LANDSCAPE, true],
            [MediaBreakpoints.TABLET_PORTRAIT, true],
            [MediaBreakpoints.TABLET_LANDSCAPE, true],
            [MediaBreakpoints.DESKTOP_PORTRAIT, false],
            [MediaBreakpoints.DESKTOP_LANDSCAPE, false]
        ]));
    }

    getFormElementById(formElementId : string) : IFormElement {
        return this.screen.form.formElements.filter(element => element.id == formElementId)[0];
    }

    anyAddressFieldsPresent() : boolean {
        return !!(this.line1Field || this.line2Field || this.cityField || this.stateField || this.postalCodeField || this.countryField);
    }

    onScanFieldChanged(formElement: IFormElement) : void {
        let loyaltyNumber = this.screen.formGroup.value[this.loyaltyNumberField.id];
        if ( loyaltyNumber != null && this.scanStartControlCharacter != null &&
            loyaltyNumber.startsWith(this.scanStartControlCharacter)) {
            console.info(`Stripping the scan control character from the start ${loyaltyNumber}`) ;
            formElement.value = loyaltyNumber.substr(this.scanStartControlCharacter.length);
            // Update the stripped string into the form control as well
            const patchGroup = {};
            patchGroup[formElement.id] = formElement.value;
            this.screen.formGroup.patchValue(patchGroup);
        }
        this.onFieldChanged(formElement);
    }

    onFieldChanged(formElement: IFormElement) : void {
        if (formElement.valueChangedAction) {
            let form = this.formBuilder.buildFormPayload(this.screen.formGroup, this.screen.form);
            this.doAction(formElement.valueChangedAction, form);
        }
    }

    submitForm() : void {
        this.formBuilder.buildFormPayload(this.screen.formGroup, this.screen.form);
        this.doAction(this.screen.submitButton, this.screen.form);
    }

    buildScreen() : void {
        if(this.screen.isStructuredForm) {
            this.buildStructuredForm();
        }

        this.screen.formGroup = this.formBuilder.group(this.screen.form);
    }

    private buildStructuredForm(): void {
        this.handledFormFields = [
            'firstName',
            'lastName',
            'loyaltyNumber',
            'phone',
            'email',
            'line1',
            'line2',
            'city',
            'state',
            'postalCode',
            'country',
            'extensionAttribute1',
        ];

        this.firstNameField = this.getFormElementById('firstName');
        this.lastNameField = this.getFormElementById('lastName');
        this.loyaltyNumberField = this.getFormElementById('loyaltyNumber');
        this.extensionAttribute1Field = this.getFormElementById('extensionAttribute1');
        this.phoneField = this.getFormElementById('phone');
        this.emailField = this.getFormElementById('email');

        this.line1Field = this.getFormElementById('line1');
        this.line2Field = this.getFormElementById('line2');
        this.cityField = this.getFormElementById('city');
        this.stateField = this.getFormElementById('state');
        this.postalCodeField = this.getFormElementById('postalCode');
        this.countryField = this.getFormElementById('country');

        if(this.line1Field) {
            this.addressIconLocationClass = 'icon1';
        } else if (this.line2Field) {
            this.addressIconLocationClass = 'icon2';
        } else if (this.cityField || this.stateField || this.postalCodeField) {
            this.addressIconLocationClass = 'icon3';
        } else if (this.countryField) {
            this.addressIconLocationClass = 'icon4';
        }

        this.phoneFields = [];
        this.phoneLabelFields = [];
        this.emailFields = [];
        this.emailLabelFields = [];
        if (this.screen && this.screen.form && this.screen.form.formElements) {
            this.screen.form.formElements.forEach(element => {
                if (element.id.match(/phonesList\d/)) {
                    this.phoneFields.push(element);
                    this.handledFormFields.push(element.id);
                }

                if (element.id.match(/phonesListLabel\d/)) {
                    this.phoneLabelFields.push(element);
                    this.handledFormFields.push(element.id);
                }

                if(element.id.match(/emailsList\d/)) {
                    this.emailFields.push(element);
                    this.handledFormFields.push(element.id);
                }

                if(element.id.match(/emailsListLabel\d/)) {
                    this.emailLabelFields.push(element);
                    this.handledFormFields.push(element.id);
                }
            });
        }
    }

}