import { ConfigChangedMessage } from './config-changed-message';

export interface ImageScannerMessage extends ConfigChangedMessage {
    scannerType: string;
}
