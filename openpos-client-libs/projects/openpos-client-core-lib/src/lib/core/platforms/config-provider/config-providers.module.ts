
import { NgModule } from '@angular/core';
import { CapacitorPlatformConfigProviderModule } from './capacitor/capacitor-platform-config.module';

@NgModule({
    imports: [
        CapacitorPlatformConfigProviderModule
    ],
    exports: [
        CapacitorPlatformConfigProviderModule
    ]
})
export class ConfigProvidersModule { }
