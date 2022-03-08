import { Injectable } from '@angular/core';
import { IActionItem } from '../actions/action-item.interface';
import { KeybindingParserService } from './keybinding-parser.service';

/**
 * Crawls arbitrary object hierarchies and finds all actions with keybindings
 */
@Injectable({
    providedIn: 'root',
})
export class KeybindingPropertyCrawlerService {
    constructor(private keybindingParser: KeybindingParserService) {
    }

    findKeybindings(obj: any): IActionItem[] {
        if (!this.isCrawlable(obj)) {
            return [];
        }

        const keybindings: IActionItem[] = [];

        if (this.hasKeybinding(obj)) {
            this.pushDistinctKeyBinding(keybindings, obj);
        }

        Object.keys(obj).forEach(key => {
            this.findKeybindings(obj[key])
                .forEach(keybinding => this.pushDistinctKeyBinding(keybindings, keybinding));
        });

        return keybindings;
    }

    hasKeybinding(obj: any): obj is { action: string, keybind: string } {
        return obj && obj.action && obj.keybind;
    }

    isCrawlable(obj: any): boolean {
        return obj !== null && obj !== undefined && typeof obj === 'object';
    }

    pushDistinctKeyBinding(keybindings: IActionItem[], keybinding: IActionItem): void {
        const keybindingsForActionItem = this.keybindingParser.splitKeys(keybinding.keybind);
        keybindingsForActionItem.forEach(keybind => {
            const index = keybindings.findIndex(k => this.keybindingParser.areEqual(k.keybind, keybind));

            // Keep the first one if we encounter a duplicate action.
            //
            // Recursion goes top to bottom, and keybindings on the top level most likely belong to the current screens,
            // as opposed to a duplicate action nested deeper down that most likely would belong to a screen part.
            if (index < 0) {
                const updatedKeybinding = Object.assign({}, keybinding);
                updatedKeybinding.keybind = keybind;
                keybindings.push(updatedKeybinding);
            }
        });
    }
}
