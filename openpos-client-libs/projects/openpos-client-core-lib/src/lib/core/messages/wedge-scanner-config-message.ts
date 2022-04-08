import { ConfigChangedMessage } from './config-changed-message';

export class WedgeScannerConfigMessage extends ConfigChangedMessage {
    startSequence: string;
    endSequence: string;
    codeTypeLength: number;
    timeout: number;
    acceptKeys: string[];
    enabled: boolean;
}
