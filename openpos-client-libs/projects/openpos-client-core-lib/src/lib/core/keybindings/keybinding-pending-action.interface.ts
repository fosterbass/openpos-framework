import { KeybindingZone } from './keybinding-zone.interface';
import { IActionItem } from '../actions/action-item.interface';

export interface KeybindingPendingAction {
    cancel: boolean;
    domEvent: KeyboardEvent;
    zone: KeybindingZone;
    action: IActionItem;
    actionPayload: any;
}
