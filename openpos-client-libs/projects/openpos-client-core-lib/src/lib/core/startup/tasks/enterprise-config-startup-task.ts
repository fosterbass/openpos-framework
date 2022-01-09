import { StartupTask } from '../startup-task';
import { EnterpriseConfigService } from '../../platform-plugins/enterprise-config/enterprise-config.service';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class EnterpriseConfigStartupTask implements StartupTask {

    constructor(protected enterpriseConfigService: EnterpriseConfigService) {
    }

    execute(): Observable<string> {
        return this.enterpriseConfigService.initialize().pipe(
            tap(msg => console.info(msg))
        );
    }
}
