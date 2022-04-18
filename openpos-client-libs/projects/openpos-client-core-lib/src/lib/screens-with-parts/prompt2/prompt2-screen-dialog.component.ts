import { Component, Injector, OnDestroy } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { TextMaskConfig } from 'angular2-text-mask';
import { Subscription } from 'rxjs';
import createAutoCorrectedDatePipe from 'text-mask-addons/dist/createAutoCorrectedDatePipe';
import { BarcodeScanner } from '../../core/platform-plugins/barcode-scanners/barcode-scanner.service';
import { ScanData } from '../../core/platform-plugins/barcode-scanners/scanner';
import { DialogComponent } from '../../shared/decorators/dialog-component.decorator';
import { CustomDateValidator } from '../../shared/validators/custom-date-validators';
import { PosScreenDirective } from '../pos-screen/pos-screen.component';
import { BaseFormControl, DateFormatKind, DynamicDateInput, DynamicFormGroup, DynamicTextInput, PromptPlusPlusUIMessage } from './form.interface';

@DialogComponent({
    name: 'PromptPlusPlus'
})
@Component({
    templateUrl: 'prompt2-screen-dialog.component.html',
    styleUrls: [
        'prompt2-screen-dialog.component.scss'
    ]
})
export class Prompt2ScreenDialogComponent extends PosScreenDirective<PromptPlusPlusUIMessage> implements OnDestroy {
    rootFormGroup?: FormGroup;
    activeImageScanInput?: BaseFormControl;

    private _barcodeScanSubscription?: Subscription;
    private _alwaysBarcodeScanning?: boolean;

    get hasImageScannerSupport(): boolean {
        return this._barcodeScanner.hasImageScanner;
    }

    private static _makeFormComponent(item: BaseFormControl): AbstractControl {
        switch (item.type) {
            case 'text':
                if (instanceOfTextInput(item)) {
                    const validators = new Array<ValidatorFn>();

                    if (item.required) {
                        validators.push(Validators.required);
                    }

                    if (item.minimumLength) {
                        validators.push(Validators.minLength(item.minimumLength));
                    }

                    if (item.maximumLength) {
                        validators.push(Validators.maxLength(item.maximumLength));
                    }

                    if (item.pattern) {
                        validators.push(Validators.pattern(item.pattern));
                    }

                    return new FormControl(item.defaultValue || '', validators);
                } else {
                    throw new Error(`cannot create form component; type indicated 'text' but failed to parse as text type`);
                }

            case 'date':
                if (instanceOfDateInput(item)) {
                    const validators = new Array<ValidatorFn>();

                    if (item.required) {
                        validators.push(Validators.required);
                    }

                    switch (item.format) {
                        case DateFormatKind.MMDD:
                            validators.push(Validators.pattern(/\d{2}\/\d{2}/));
                            break;

                        case DateFormatKind.DDMMYY:
                        case DateFormatKind.MMDDYY:
                            validators.push(Validators.pattern(/\d{2}\/\d{2}\/\d{2}/));
                            break;

                        case DateFormatKind.DDMMYYYY:
                        case DateFormatKind.MMDDYYYY:
                            validators.push(Validators.pattern(/\d{2}\/\d{2}\/\d{4}/));
                            break;

                        default:
                            throw new Error('cannot produce proper date validation; format not found');
                    }

                    if (item.min) {
                        validators.push(CustomDateValidator.minDate(item.min));
                    }

                    if (item.max) {
                        validators.push(CustomDateValidator.maxDate(item.max));
                    }

                    return new FormControl(item.defaultValue || '', validators);
                } else {
                    throw new Error(`cannot create form component; type indicated 'date' but failed to parse as date type`);
                }

            default: throw new Error(`cannot create form component; unknown component type '${item.type}'`);
        }
    }

    constructor(injector: Injector, private _barcodeScanner: BarcodeScanner) {
        super(injector);
    }

    buildScreen(): void {
        const root = new FormGroup({});

        for (const item of this.screen.items) {
            root.addControl(item.id, Prompt2ScreenDialogComponent._makeFormComponent(item));
        }

        this.rootFormGroup = root;

        const scanControl = this.screen.items
                .filter(instanceOfTextInput)
                .filter(t => t.allowBarcodeScanEntry);

        this._alwaysBarcodeScanning = scanControl.length === 1;

        if (this._alwaysBarcodeScanning && !this.hasImageScannerSupport) {
            this._startBarcodeScanning(scanControl[0]);
        }
    }

    ngOnDestroy(): void {
        this._stopBarcodeScanning();
    }

    onFormSubmit() {
        this._stopBarcodeScanning();

        this.doAction(this.screen.submitAction, this.rootFormGroup.value);
    }

    onInputFocused(control: BaseFormControl) {
        if (instanceOfTextInput(control) && control.allowBarcodeScanEntry && !this._alwaysBarcodeScanning) {
            this._startBarcodeScanning(control);
        }
    }

    onInputBlurred() {
        if (!this._alwaysBarcodeScanning) {
            this._stopBarcodeScanning();
        }
    }

    private _startBarcodeScanning(control: BaseFormControl) {
        this._barcodeScanSubscription = this._barcodeScanner.beginScanning().subscribe(data => {
            this.acceptScanInputForControl(control, data);
        });
    }

    private _stopBarcodeScanning() {
        this.activeImageScanInput = undefined;

        if (this._barcodeScanSubscription) {
            this._barcodeScanSubscription.unsubscribe();
            this._barcodeScanSubscription = undefined;
        }
    }

    getTextMask(control: BaseFormControl): TextMaskConfig {
        if (!instanceOfDateInput(control)) {
            throw new Error(`failed to get mask for control ${control.id}; it doesn't have a format`);
        }

        switch (control.format) {
            case DateFormatKind.MMDD:
                return { mask: [/\d/, /\d/, '/', /\d/, /\d/], pipe: createAutoCorrectedDatePipe('mm/dd') };

            case DateFormatKind.MMDDYY:
                return { mask: [/\d/, /\d/, '/', /\d/, /\d/, '/', /\d/, /\d/], pipe: createAutoCorrectedDatePipe('mm/dd/yy') };

            case DateFormatKind.DDMMYY:
                return { mask: [/\d/, /\d/, '/', /\d/, /\d/, '/', /\d/, /\d/], pipe: createAutoCorrectedDatePipe('dd/mm/yy') };

            case DateFormatKind.DDMMYYYY:
                return {
                    mask: [/\d/, /\d/, '/', /\d/, /\d/, '/', /\d/, /\d/, /\d/, /\d/],
                    pipe: createAutoCorrectedDatePipe('dd/mm/yyyy')
                };

            case DateFormatKind.MMDDYYYY:
                return {
                    mask: [/\d/, /\d/, '/', /\d/, /\d/, '/', /\d/, /\d/, /\d/, /\d/],
                    pipe: createAutoCorrectedDatePipe('mm/dd/yyyy')
                };
        }
    }

    controlSupportsBarcodeScanInput(control: BaseFormControl): boolean {
        return this.hasImageScannerSupport
                && instanceOfTextInput(control)
                && control.allowBarcodeScanEntry;
    }

    toggleScanForControl(control: BaseFormControl) {
        this.activeImageScanInput = this.controlSupportsBarcodeScanInput(control) && this.activeImageScanInput !== control
                ? control
                : undefined;
    }

    acceptScanInputForControl(control: BaseFormControl, scan: ScanData) {
        this._stopBarcodeScanning();

        if (instanceOfTextInput(control)) {
            this.rootFormGroup.patchValue({
                [control.id]: scan.data
            });

            // submit on scan will only work if there is only a single
            // input that allows for scanned data.
            const onlyScannable = this.screen.items
                    .filter(instanceOfTextInput)
                    .filter(t => t.allowBarcodeScanEntry)
                    .length === 1;

            if (this.screen.submitOnScan && onlyScannable) {
                if (this.screen.submitOnScanAction) {
                    this.doAction(this.screen.submitOnScanAction, scan);
                } else if (this.rootFormGroup.valid) {
                    this.onFormSubmit();
                }
            }
        } else {
            throw new Error('only text controls can accept scan input');
        }
    }

    onDatePickerPicked(control: BaseFormControl, date: Date) {
        this.rootFormGroup.patchValue({
            [control.id]: this.maskDate(control, date)
        });

        // since date selection happens with a hidden input, we
        // need to let the form validation know that the real
        // input we filled out had data entered into and was
        // implicitly "touched". This makes form validation show
        // up.
        this.rootFormGroup.controls[control.id].markAsTouched();
        this.rootFormGroup.controls[control.id].markAsDirty();
    }

    maskDate(control: BaseFormControl, date: Date): string {
        if (!instanceOfDateInput(control)) {
            throw new Error('can only mask date inputs');
        }

        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const year = date.getFullYear().toString();

        switch (control.format) {
            case DateFormatKind.DDMMYY:
                return `${day}/${month}/${year.substring(2)}`;

            case DateFormatKind.DDMMYYYY:
                return `${day}/${month}/${year}`;

            case DateFormatKind.MMDD:
                return `${month}/${day}`;

            case DateFormatKind.MMDDYY:
                return `${month}/${day}/${year.substring(2)}`;

            case DateFormatKind.MMDDYYYY:
                return `${month}/${day}/${year}`;
        }
    }

    getDatePlaceholder(control: BaseFormControl): string {
        if (!instanceOfDateInput(control)) {
            throw new Error('placeholder only works for dates');
        }

        switch (control.format) {
            case DateFormatKind.DDMMYY: return 'DD/MM/YY';
            case DateFormatKind.DDMMYYYY: return 'DD/MM/YYYY';
            case DateFormatKind.MMDD: return 'MM/DD';
            case DateFormatKind.MMDDYY: return 'MM/DD/YY';
            case DateFormatKind.MMDDYYYY: return 'MM/DD/YYYY';
        }
    }
}

function instanceOfFormGroup(item: any): item is DynamicFormGroup {
    return item.type === 'group' && !!item.children;
}

function instanceOfTextInput(item: any): item is DynamicTextInput {
    return item.type === 'text' && item.id && item.label;
}

function instanceOfDateInput(item: any): item is DynamicDateInput {
    return item.type === 'date' && item.format;
}
