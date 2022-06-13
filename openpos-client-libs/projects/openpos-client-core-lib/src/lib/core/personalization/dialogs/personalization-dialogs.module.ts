import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { DeviceEntryComponent } from './device-entry.component';
import { PersonalizationEntryComponent } from './personalization-entry.component';
import { ServerConnectComponent } from './server-connect.component';
import { ServerEntryComponent } from './server-entry.component';

@NgModule({
    imports: [
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatCheckboxModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        MatIconModule,
        MatStepperModule,
        MatSelectModule,
        ReactiveFormsModule,
        CommonModule
    ],
    declarations: [
        ServerEntryComponent,
        ServerConnectComponent,
        PersonalizationEntryComponent,
        DeviceEntryComponent
    ],
    exports: [
        ServerEntryComponent,
        ServerConnectComponent,
        PersonalizationEntryComponent
    ],
    entryComponents: [
        ServerEntryComponent,
        ServerConnectComponent,
        PersonalizationEntryComponent,
        DeviceEntryComponent
    ]
})
export class PersonalizationDialogsModule {}
