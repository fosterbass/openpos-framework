import { ConfigChangedMessage } from './config-changed-message';

export interface ScanditCapacitorMessage extends ConfigChangedMessage {
    licenseKey: string;
}
