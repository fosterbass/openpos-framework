import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { BusinessUnitDevice, PersonalizationConfig } from '../personalization-config';

export interface DeviceInputContext {
    businessUnit: string;

    // if device id is set, then this will be considered an edit
    // and this field will be read-only
    deviceId?: string;

    config: PersonalizationConfig;
}

@Component({
    templateUrl: './device-entry.component.html',
    styleUrls: ['./device-entry.component.scss']
})
export class DeviceEntryComponent implements OnInit {
    private static DEVICE_ID_PATTERN = /^(?<bu>\d{5})-(?<num>\d{3})$/;

    formGroup: FormGroup;
    readOnlyDeviceId = false;

    get deviceNumber() {
        return this.formGroup.get('deviceNumber');
    }

    get appId() {
        return this.formGroup.get('appId');
    }

    get additionalParams() {
        return this.formGroup.get('additionalParams') as FormGroup;
    }

    constructor(
        private ref: MatDialogRef<DeviceEntryComponent, BusinessUnitDevice>,
        @Inject(MAT_DIALOG_DATA) public data: DeviceInputContext
    ) {}

    ngOnInit(): void {
        const deviceId = this.data.deviceId;
        let defaultDeviceNumber: string | undefined;
        let existingDevice: BusinessUnitDevice | undefined;

        const deviceNumValidators = [
            Validators.required,
            Validators.pattern(/^\d{3}$/)
        ];

        if (deviceId) {
            this.readOnlyDeviceId = true;

            const storeDevices = this.data.config.storeDevices[this.data.businessUnit];
            if (storeDevices) {
                existingDevice = storeDevices.find(d => d.deviceId === deviceId);
            }

            const match = deviceId.match(DeviceEntryComponent.DEVICE_ID_PATTERN);
            defaultDeviceNumber = match.groups.num;
        } else {
            deviceNumValidators.push(this.mustBeUniqueDevice());
        }

        let defaultAppId = existingDevice?.appId;
        if (!defaultAppId) {
            defaultAppId = this.data.config.loadedAppIds?.find(a => a === 'pos')
                ? 'pos'
                : undefined;
        }

        this.formGroup = new FormGroup({
            deviceNumber: new FormControl(defaultDeviceNumber, deviceNumValidators),
            appId: new FormControl(defaultAppId, [Validators.required]),
            additionalParams: new FormGroup({})
        });

        this.data.config.parameters.forEach(param => {
            let defaultValue = param.defaultValue;

            if (existingDevice) {
                const existingValue = existingDevice.personalizationParamValues[param.property];

                if (existingValue) {
                    defaultValue = existingValue;
                }
            }

            this.additionalParams.addControl(param.property, new FormControl(defaultValue));
        });
    }

    getAdditionalParam(id: string) {
        return this.additionalParams.get(id);
    }

    onSubmit() {
        if (!this.formGroup.valid) {
            return;
        }

        const deviceNumber = this.deviceNumber;
        if (!deviceNumber || !deviceNumber.valid) {
            return;
        }

        const deviceId = `${this.data.businessUnit}-${deviceNumber.value}`;

        const appId = this.appId;
        if (!appId || !appId.valid) {
            return;
        }

        this.ref.close({
            businessUnitId: this.data.businessUnit,
            deviceId,
            appId: appId.value,
            personalizationParamValues: this.additionalParams.value
        });
    }

    onCancel() {
        this.ref.close();
    }

    deviceNumberEntryHintDisplay(): string | null {
        if (this.deviceNumber.hasError('required')) {
            return 'The Device ID must be specified';
        }

        if (this.deviceNumber.hasError('pattern')) {
            return 'Please enter a 3-digit sequence (eg. 001)';
        }

        if (this.deviceNumber.hasError('uniqueDevice')) {
            return this.deviceNumber.getError('uniqueDevice');
        }

        return null;
    }

    private mustBeUniqueDevice(): ValidatorFn {
        const bu = this.data.businessUnit;
        const devices = this.data.config.storeDevices[bu];

        return (control: AbstractControl) => {
            if (!devices) {
                return null;
            }

            const deviceId = `${bu}-${control.value}`;

            const existingDevice = devices.find(d => d.deviceId === deviceId);
            if (existingDevice) {
                return {
                    uniqueDevice: `Device ID '${deviceId}' is already in use`
                };
            }

            return null;
        };
    }
}
