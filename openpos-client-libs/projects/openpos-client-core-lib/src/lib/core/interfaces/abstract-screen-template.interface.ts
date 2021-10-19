import { ISystemStatus } from './system-status.interface';

export interface IAbstractScreenTemplate {
    type: string;
    dialog: boolean;
    systemStatus: ISystemStatus;
}
