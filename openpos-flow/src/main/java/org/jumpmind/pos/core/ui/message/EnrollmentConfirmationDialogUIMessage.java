package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.messagepart.DialogHeaderPart;
import org.jumpmind.pos.core.ui.messagepart.MessagePartConstants;

@Data
public class EnrollmentConfirmationDialogUIMessage extends UIMessage {

    private String programCopy;
    private String benefactorName;
    private ActionItem continueAction;
    private ActionItem signUpMorePets;
    private String message;
    private String checkIcon;

    public EnrollmentConfirmationDialogUIMessage() {
        setId("EnrollmentConfirmationDialog");
        setScreenType("EnrollmentConfirmationDialog");
    }

    public void setTitle(String title) {
        DialogHeaderPart dialogHeader = getDialogHeaderPart();
        dialogHeader.setHeaderText(title);
    }

    public DialogHeaderPart getDialogHeaderPart() {
        DialogHeaderPart dialogHeader = (DialogHeaderPart) get(MessagePartConstants.DialogHeader);
        if (dialogHeader == null) {
            dialogHeader = new DialogHeaderPart();
            addMessagePart(MessagePartConstants.DialogHeader, dialogHeader);
        }
        return dialogHeader;
    }
}
