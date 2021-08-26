import { IActionItem } from '../../../core/actions/action-item.interface';

export interface DynamicBaconStripInterface {
    deviceId: string;
    operatorLine1: string;
    operatorLine2: string;
    operatorMenu: IActionItem[];
    operatorIcon: string;
    headerText: string;
    logo: string;
    actions: IActionItem[];
    icon: string;
}
