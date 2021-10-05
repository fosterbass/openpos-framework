import { ConfigChangedMessage } from "./config-changed-message";

export class UIConfigMessage extends ConfigChangedMessage {
    configType = 'uiConfig';
    enableKeybinds: 'true' | 'false';
    googleApiKey: string;
    showStatusBar: 'true' | 'false';
}
