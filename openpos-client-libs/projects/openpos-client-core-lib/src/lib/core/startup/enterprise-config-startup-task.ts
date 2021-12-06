import {IStartupTask} from "./startup-task.interface";
import {StartupTaskNames} from "./startup-task-names";
import {StartupTaskData} from "./startup-task-data";
import {Observable} from "rxjs";
import {EnterpriseConfigService} from "../platform-plugins/enterprise-config/enterprise-config.service";


export class EnterpriseConfigStartupTask implements IStartupTask {
    name =  StartupTaskNames.ENTERPRISE_CONFIG;
    order = 250;

    constructor(protected enterpriseConfigService: EnterpriseConfigService) {
    }

    execute(data?: StartupTaskData): Observable<string> {
        return this.enterpriseConfigService.initialize();
    }
}