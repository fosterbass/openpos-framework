import { IActionItem } from '../actions/action-item.interface';

export interface KeybindingActionCache {
    [key: string]: IActionItem;
}
