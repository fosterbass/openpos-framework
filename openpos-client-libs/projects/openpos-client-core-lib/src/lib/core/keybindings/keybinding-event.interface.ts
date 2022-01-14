import { IActionItem } from '../actions/action-item.interface';
import { KeybindingZone } from './keybinding-zone.interface';

export interface KeybindingEvent {
    domEvent: KeyboardEvent;
    zone: KeybindingZone;
    action?: IActionItem;
    didDoAction: boolean;
}
