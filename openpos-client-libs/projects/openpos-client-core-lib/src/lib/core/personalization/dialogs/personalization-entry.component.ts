import { Component, Inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AbstractControl, Form, FormControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { Observable, timer } from 'rxjs';
import { mergeMap, retryWhen, tap, timeout } from 'rxjs/operators';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ServerEntryData } from './server-entry.component';
import { BusinessUnitDevice, PersonalizationConfig } from '../personalization-config';
import { DeviceEntryComponent, DeviceInputContext } from './device-entry.component';

@Component({
    templateUrl: './personalization-entry.component.html',
    styleUrls: ['./personalization-entry.component.scss']
})
export class PersonalizationEntryComponent implements OnInit {
    businessUnitSelectionGroup: FormGroup;
    existingDeviceSelectionGroup: FormGroup;

    config: Observable<PersonalizationConfig>;

    get selectedBusinessUnitId(): AbstractControl {
        return this.businessUnitSelectionGroup.get('businessUnit');
    }

    get selectedDeviceId(): AbstractControl {
        return this.existingDeviceSelectionGroup.get('device');
    }

    private tappedConfig?: PersonalizationConfig;

    constructor(
        private http: HttpClient,
        private dialog: MatDialog,
        private ref: MatDialogRef<PersonalizationEntryComponent, BusinessUnitDevice>,
        @Inject(MAT_DIALOG_DATA) private serverData: ServerEntryData
    ) {}

    ngOnInit(): void {
        this.businessUnitSelectionGroup = new FormGroup({
            businessUnit: new FormControl(undefined, [Validators.required])
        });

        this.businessUnitSelectionGroup.valueChanges.subscribe(() => {

            // issue where a change is happening while in the onChecked lifecycle; dispatch out
            // the reset to get it out of the lifecycle.
            setTimeout(() => {
                this.existingDeviceSelectionGroup.reset();
            });
        });

        this.existingDeviceSelectionGroup = new FormGroup({
            device: new FormControl(undefined, [Validators.required, this.deviceNotConnected()]),
        });

        const protocol = !!this.serverData.secure ? 'https://' : 'http://';
        let portString = ':' + this.serverData.port.toString();

        if (!!this.serverData.secure && this.serverData.port === 443) {
            portString = '';
        } else if (!this.serverData.secure && this.serverData.port === 80) {
            portString = '';
        }

        this.config = this.http.get<PersonalizationConfig>(protocol + this.serverData.host + portString + '/rest/devices/personalizationConfig').pipe(
            timeout(10000),
            retryWhen(errors => errors.pipe(
                mergeMap(() => timer(5000))
            )),
            tap(c => {
                if (c.availableBusinessUnits && c.availableBusinessUnits.length === 1) {
                    this.businessUnitSelectionGroup.setValue({
                        businessUnit: c.availableBusinessUnits[0].id
                    });
                }

                this.tappedConfig = c;

                if (c.autoPersonalizationToken) {
                    this.ref.close({
                        authToken: c.autoPersonalizationToken
                    });
                }
            }),
        );
    }

    getDevicesForBusinessUnit(personalization: PersonalizationConfig, bu?: string): BusinessUnitDevice[] {
        if (!!bu && personalization.storeDevices[bu]) {
            return personalization.storeDevices[bu];
        }

        return [];
    }

    onDeviceSelectionFormSubmitted(config: PersonalizationConfig) {
        const businessUnit: string = this.selectedBusinessUnitId?.value;
        if (!businessUnit) {
            return;
        }

        const deviceValue: string = this.selectedDeviceId.value;
        const storeDevices: BusinessUnitDevice[] = config.storeDevices[businessUnit];

        if (!storeDevices) {
            return;
        }

        const device = storeDevices.find(d => d.deviceId === deviceValue);
        if (device) {
            this.ref.close(device);
        }
    }

    createErrorHint(control: AbstractControl): string | null {
        if (control.hasError('required')) {
            return 'Required';
        }

        if (control.hasError('deviceConnected')) {
            return control.getError('deviceConnected');
        }

        return null;
    }

    async openDeviceEntryComponent(context: DeviceInputContext): Promise<BusinessUnitDevice | undefined> {
        const editDialogRef = this.dialog.open(
            DeviceEntryComponent,
            {
                hasBackdrop: true,
                panelClass: 'openpos-default-theme',
                width: '65vw',
                minWidth: '200px',
                maxWidth: '600px',
                data: context
            }
        );

        return await editDialogRef.afterClosed().toPromise() as BusinessUnitDevice | undefined;
    }

    async onEditDevice(config: PersonalizationConfig, bu: string, deviceId: string) {
        const context: DeviceInputContext = {
            businessUnit: bu,
            deviceId,
            config
        };

        const result = await this.openDeviceEntryComponent(context);
        if (result) {
            const storeDevices = config.storeDevices[bu];

            let deviceIndex = -1;
            const existingDevice = storeDevices.find((d, index) => {
                if (d.deviceId === deviceId) {
                    deviceIndex = index;
                    return true;
                }

                return false;
            });

            if (deviceIndex < 0) {
                return;
            }

            storeDevices[deviceIndex] = {
                // unmodifiable fields
                businessUnitId: existingDevice.businessUnitId,
                deviceId: existingDevice.deviceId,
                authToken: existingDevice.authToken,

                // modifiable fields
                appId: result.appId,
                personalizationParamValues: result.personalizationParamValues
            };
        }
    }

    async onCreateNewDevice(config: PersonalizationConfig, bu: string) {
        const context: DeviceInputContext = {
            businessUnit: bu,
            config
        };

        const result = await this.openDeviceEntryComponent(context);
        if (result) {
            let storeDevices = config.storeDevices[bu];
            if (!storeDevices) {
                storeDevices = new Array<BusinessUnitDevice>();
            }

            storeDevices.push(result);
            config.storeDevices[bu] = storeDevices;

            this.existingDeviceSelectionGroup.patchValue({
                device: result.deviceId
            });
        }
    }

    private deviceNotConnected(): ValidatorFn {
        return (control: AbstractControl) => {
            const sbu: string = this.selectedBusinessUnitId?.value;

            if (!sbu) {
                return null;
            }

            if (!this.tappedConfig) {
                return null;
            }

            const storeDevices = this.tappedConfig.storeDevices[sbu];
            if (!storeDevices || storeDevices.length <= 0) {
                return null;
            }

            const deviceId: string = control.value;
            const device = storeDevices.find(d => d.deviceId === deviceId);
            if (!device || !device.connected) {
                return null;
            }

            return {
                deviceConnected: `A connection is already established to '${deviceId}'`
            };
        };
    }
}
