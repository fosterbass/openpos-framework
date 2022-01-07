import { Inject, Injectable, InjectionToken, Optional } from '@angular/core';
import { IEnterpriseConfig } from './enterprise-config.interface';
import { concat, defer, iif, merge, Observable, of } from 'rxjs';

export const ENTERPRISE_CONFIGS = new InjectionToken<IEnterpriseConfig[]>('EnterpriseConfigs');

@Injectable({providedIn: 'root'})
export class EnterpriseConfigService {

    private initialized = false;
    private combinedConfig = {};

    constructor(@Optional() @Inject(ENTERPRISE_CONFIGS) private enterpriseConfigs: Array<IEnterpriseConfig>) {
    }

    public initialize(): Observable<string>  {
        if (! this.initialized) {
            const filterConfigs =  new Observable<string>( messages => {
                // remove configs that are not active/present
                const configsToRemove = [];

                messages.next(`${this.enterpriseConfigs.length} enterprise configs found to try`);
                this.enterpriseConfigs.forEach( cfg => {
                    if ( ! cfg.configPresent() ) {
                        configsToRemove.push(cfg);
                        messages.next(`Removing ${cfg.configName}`);
                    } else {
                        messages.next(`Found ${cfg.configName}`);
                    }
                });

                configsToRemove.forEach( cfg => {
                    this.enterpriseConfigs.splice( this.enterpriseConfigs.indexOf(cfg), 1);
                });
                messages.complete();
            });

            const initializeConfigs = new Observable<string>( messages => {
                const inits = this.enterpriseConfigs.map( cfg => {
                    return concat(
                        of(`Initializing ${cfg.configName}`),
                        cfg.initialize()
                    );
                });

                merge( ...inits ).subscribe( {
                    next: m => messages.next(m),
                    error: e => {
                        console.error(`[EnterpriseConfigService] Failed to initialize a configuration. Error: ${JSON.stringify(e)}`);
                        messages.next(`Failed to initialize a configuration: ${JSON.stringify(e)}`);
                    },
                    complete: () => {
                        messages.complete();
                    }
                });
            });


            return concat(
                filterConfigs,
                iif( () => !!this.enterpriseConfigs && this.enterpriseConfigs.length > 0,
                    concat (
                        of(`Initializing ${this.enterpriseConfigs.length} Enterprise Configuration(s)...`),
                        initializeConfigs,
                        this.combineConfigurations()
                    ),
                    of('No Enterprise Configurations found')
                ),
                defer(() => {
                    this.initialized = true;
                    return of('Enterprise Configurations initialized');
                })
            );

        } else {
            return of('Enterprise Configurations are already initialized');
        }
    }

    private combineConfigurations(): Observable<string> {
        return new Observable<string>(messages => {
            if (this.enterpriseConfigs) {
                if (this.enterpriseConfigs.length > 1) {
                    messages.next('Combining Enterprise Configurations...');
                }

                const configs = this.enterpriseConfigs.map(eCfg => new Observable<string>(observer => {
                    eCfg.getConfiguration().subscribe( cfgData => {
                        this.addConfiguration(eCfg, cfgData, observer);
                        observer.complete();
                    });
                }));

                concat(...configs).subscribe({
                    complete: () => messages.complete(),
                    error: (err) => messages.error(err),
                    next: (msg) => messages.next(msg)
                });

            } else {
                messages.complete();
            }

        });
    }

    public getConfiguration(): any {
        return this.combinedConfig;
    }

    public getString(configName: string): string {
        const value = this.combinedConfig[configName];
        if (value) {
            return typeof value === 'string' ? value : value.toString();
        } else {
            return value;
        }
    }

    private addConfiguration(enterpriseConfig, configData, observer) {
        if (configData) {
            try {
                this.combinedConfig = Object.assign(this.combinedConfig, JSON.parse(configData));
            } catch (e) {
                observer.error(`Failed to parse config from ${enterpriseConfig.configName} into JSON. config: ${configData}`);
            }
        }
        if (this.enterpriseConfigs.length > 1) {
            observer.next(`Added/overlaid configuration from ${enterpriseConfig.configName}`);
        }
    }
}
