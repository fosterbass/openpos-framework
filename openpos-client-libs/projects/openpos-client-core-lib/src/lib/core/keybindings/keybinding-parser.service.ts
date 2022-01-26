import { Injectable } from '@angular/core';
import { KeybindingKey } from './keybinding-key.interface';
import { KeybindingLikeKey } from './keybinding-like-key.interface';

/**
 * Parses keybinding strings, like Enter, Ctrl+Shift+F10 and ArrowUp,ArrowDown etc.
 */
@Injectable({
    providedIn: 'root',
})
export class KeybindingParserService {
    private keyRegex: RegExp;

    keyDelimiter = ',';
    keyEscape = '\\';
    keyCombinationChar = '+';

    constructor() {
        try {
            // Matches a single key like "p" or "ctrl+p"
            this.keyRegex = new RegExp(/(?<key>(\\\+|[^+])+)/, 'g');
        } catch (e) {
            // This was observed on Android emulator:
            // Invalid regular expression: /(?<key>(\\\+|[^+])+)/: Invalid group
            console.error(`keyRegex failed to load: ${e.message}`);
        }
    }

    getNormalizedKey(obj: KeybindingLikeKey): string {
        const keyBinding = typeof obj === 'string' ? this.parse(obj as string)[0] : obj as KeybindingKey;
        let normalizedKey = '';

        if (!keyBinding) {
            return normalizedKey;
        }

        // When the user autocompletes the form with the browser there's no "key" property on the KeyboardEvent
        if (!keyBinding.key) {
            return 'browser-autocomplete';
        }

        if (keyBinding.key !== 'Control') {
            normalizedKey += (keyBinding.ctrlKey ? 'ctrl+' : '');
        }
        if (keyBinding.key !== 'Alt') {
            normalizedKey += (keyBinding.altKey ? 'alt+' : '');
        }
        if (keyBinding.key !== 'Shift') {
            normalizedKey += (keyBinding.shiftKey ? 'shift+' : '');
        }
        if (keyBinding.key !== 'Meta') {
            normalizedKey += (keyBinding.metaKey ? 'meta+' : '');
        }
        normalizedKey += this.escapeKey(keyBinding.key);

        return normalizedKey.toLowerCase();
    }

    areEqual(objA: KeybindingLikeKey, objB: KeybindingLikeKey): boolean {
        return this.getNormalizedKey(objA) === this.getNormalizedKey(objB);
    }

    hasKey(obj: KeybindingLikeKey, key: string): boolean {
        const keys = this.parse(key);
        // There could be a comma-separated list of keys so check if at least one matches the object
        return keys.some(k => this.areEqual(obj, k));
    }

    parse(key: string): KeybindingKey[] {
        if (!key) {
            return [];
        }

        const keys = this.splitKeys(key);
        const keyBindings = [];

        keys.forEach((theKey: string) => {
            const keyParts =
                Array.from(
                    theKey.matchAll(this.keyRegex)
                ).map((value: RegExpMatchArray) => value.groups.key);

            if (keyParts.length === 0) {
                return;
            }

            const keyBinding: KeybindingKey = {
                key: this.unescapeKey(keyParts[keyParts.length - 1].toLowerCase())
            };

            for (let i = 0; i < keyParts.length - 1; i++) {
                const keyPart = keyParts[i].toLowerCase();

                // Being flexible with how developers want to specify key modifiers
                switch (keyPart) {
                    case 'command':
                    case 'cmd':
                    case 'mta':
                    case 'met':
                    case 'meta':
                        keyBinding.metaKey = true;
                        break;
                    case 'alt':
                    case 'opt':
                    case 'option':
                    case 'optn':
                        keyBinding.altKey = true;
                        break;
                    case 'ctl':
                    case 'ctr':
                    case 'ctrl':
                    case 'control':
                        keyBinding.ctrlKey = true;
                        break;
                    case 'shft':
                    case 'sft':
                    case 'shift':
                        keyBinding.shiftKey = true;
                        break;
                }
            }

            keyBindings.push(keyBinding);
        });

        return keyBindings;
    }

    /**
     * Splits key presses separated by a comma
     * @param keys The set of one or more keys to split
     * @example
     * p
     * ctrl+p
     * ctrl+p,ctrl+a,cmd+\,,p
     */
    splitKeys(keys: string): string[] {
        if (!keys) {
            return [];
        }

        const keyPressList = [];
        let keyBuffer = '';

        for (let i = 0; i < keys.length; i++) {
            const char = keys[i];
            const nextChar = keys[i + 1];

            // If the delimiter is escaped, treat it as a key
            if (char === this.keyEscape && (nextChar === this.keyDelimiter || nextChar === this.keyCombinationChar)) {
                keyBuffer += this.keyEscape + nextChar;
                i++;
                // If we've reached the delimiter, and there's stuff in the buffer, add the buffer to the key list and flush buffer
            } else if (char === this.keyDelimiter && keyBuffer) {
                keyPressList.push(keyBuffer);
                keyBuffer = '';
                // Add the char to the key buffer
            } else {
                keyBuffer += char;
            }
        }

        // Add what's left in the key buffer to the list
        if (keyBuffer) {
            keyPressList.push(keyBuffer);
        }

        return keyPressList;
    }

    unescapeKey(key: string): string {
        return key.startsWith('\\') ? key.substr(1) : key;
    }

    escapeKey(key: string): string {
        if (key === this.keyCombinationChar || key === this.keyDelimiter) {
            return this.keyEscape + key;
        }

        return key;
    }
}
