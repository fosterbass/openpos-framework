import { IActionItem } from '../actions/action-item.interface';
import { ActionService } from '../actions/action.service';

export interface KeybindingZone {
    // Unique id for the zone
    id?: string;

    // Make this always active regardless of which zone is active
    alwaysActive?: boolean;

    // Object to crawl properties and add to actions list
    actionsObj?: any;

    // Optional list of actions with keybindings
    actions?: IActionItem[];

    // The action service to use for auto-executing actions for keybindings
    // This can be removed if the ActionService becomes a singleton
    actionService?: ActionService;

    // Automatically run an action that matches the pressed key (true by default)
    autoDoAction?: boolean;
}
