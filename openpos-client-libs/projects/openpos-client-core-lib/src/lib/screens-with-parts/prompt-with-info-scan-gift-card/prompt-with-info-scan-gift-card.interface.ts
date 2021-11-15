import {PromptWithInfoInterface} from '../prompt-with-info/prompt-with-info.interface';

export interface PromptWithInfoScanGiftCardInterface extends PromptWithInfoInterface {
    isRequired: boolean;
    isScanGiftCardEnabled: boolean;
}
