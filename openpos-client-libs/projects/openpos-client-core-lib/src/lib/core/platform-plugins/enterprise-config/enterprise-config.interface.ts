import {Observable} from 'rxjs';

export interface IEnterpriseConfig {
    /**
     * Unique name to identify this configuration
     */
    configName: string;

    /**
     * Creates an Observable task that will initialize the plugin, stream back
     * status messages and complete when done.
     */
    initialize(): Observable<string>;

    /**
     * Reports whether or not this configuration is runtime available for usage. On certain
     * platforms or deployment scenarios, it may not be available.
     */
    configPresent(): boolean;

    /**
     * Returns the entire configuration available as a javascript object.
     */
    getConfiguration(): Observable<any>;

    /**
     * Returns the string value associated with the given configName
     * @param configName The name of the string value to return
     */
    getString(configName: string): Observable<string>;
}
