import { IActionItem } from '../../../core/actions/action-item.interface';

export interface DialogHeaderInterface {
    /**
     * Text to display at the top of the screen
     */
    headerText: string;
    /**
     * Optional additional text to display under the header text
     */
    headerAdditionalText?: string;
    /**
     * Name of the Icon to show next to the header text
     */
    headerIcon: string;

    headerContextStyle: string;
    headerContextText: string;

    /**
     * Shows the X button in the top right corner of the dialog
     *  [action-item.interface.ts](../../../core/interfaces/action-item.interface.ts)
     */
    backButton: IActionItem;
}
