import { ConfigChangedMessage } from './config-changed-message';

export interface CapacitorMessage extends ConfigChangedMessage {
    licenseKey: string;
}
