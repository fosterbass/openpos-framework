import { Inject, Injectable, InjectionToken, Optional } from '@angular/core';
import { EMPTY, merge, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ImageScanner, IMAGE_SCANNERS, Scanner, SCANNERS } from '../../platform-plugins/barcode-scanners/scanner';
import { IPlatformPlugin, PLUGINS } from '../../platform-plugins/platform-plugin.interface';
import { StartupTask } from '../startup-task';

@Injectable({
    providedIn: 'root'
})
export class PlatformPluginsStartupTask implements StartupTask {
    constructor(
        @Optional() @Inject(PLUGINS)
        private _plugins: IPlatformPlugin[],

        @Optional() @Inject(SCANNERS)
        private _scanners: Scanner[],

        @Optional() @Inject(IMAGE_SCANNERS)
        private _imageScanners: ImageScanner[]
    ) { }

    execute(): Observable<void> {
        function isRemovableScanner(scanners: Scanner[], inst: any): inst is Scanner {
            return inst.beginScanning && scanners.includes(inst);
        }

        function isRemovableImageScanner(scanners: ImageScanner[], inst: any): inst is ImageScanner {
            return inst.name && inst.beginScanning && scanners.includes(inst);
        }

        if (!this._plugins) {
            console.warn('plugins list not configured');
            return EMPTY;
        }

        const unsupportedPlugins = this._plugins.filter(p => !p.pluginPresent());

        if (unsupportedPlugins.length > 0) {
            console.log(`unsupported platform plugins [${unsupportedPlugins.map(x => x.name()).join(', ')}]; removing...`);

            for (const plugin of unsupportedPlugins) {
                this._plugins.splice(this._plugins.indexOf(plugin), 1);

                if (this._scanners && isRemovableScanner(this._scanners, plugin)) {
                    this._scanners.splice(this._scanners.indexOf(plugin), 1);
                }

                if (this._imageScanners && isRemovableImageScanner(this._imageScanners, plugin)) {
                    this._scanners.splice(this._imageScanners.indexOf(plugin), 1);
                }
            }

            unsupportedPlugins.forEach(plugin => {
                this._plugins.splice(this._plugins.indexOf(plugin), 1);
            });
        }

        if (this._plugins.length > 0) {
            console.log(`initializing plugins [${this._plugins.map(p => p.name()).join(', ')}]`);
            return merge(...this._plugins.map(p => p.initialize())).pipe(
                map(() => { })
            );
        }

        return EMPTY;
    }
}
