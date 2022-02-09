package org.jumpmind.pos.core.ui.message;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.messagepart.DialogHeaderPart;
import org.jumpmind.pos.core.ui.messagepart.MessagePartConstants;
import org.jumpmind.pos.util.StackTraceUtils;

@Data
@Slf4j
public class ErrorDialogUIMessage extends UIMessage {
    String message;
    String imageUrl;
    String altImageUrl;
    ActionItem button;

    public ErrorDialogUIMessage() {
        // Logging for troubleshooting empty error message in the logs
        log.info("Created with empty constructor ErrorDialogUIMessage() - Stack trace: {}",
                StackTraceUtils.formatStackTrace(Thread.currentThread().getStackTrace()));
    }

    @Builder
    public ErrorDialogUIMessage(ActionItem button, String message, String title, String imageUrl) {
        super("ErrorDialog", "ErrorDialog");

        // Logging for troubleshooting empty error message in the logs
        log.info("Created with constructor ErrorDialogUIMessage(button: {}, message: {}, title: {}, imageUrl: {}) - Stack trace: {}",
                button, message, title, imageUrl,
                StackTraceUtils.formatStackTrace(Thread.currentThread().getStackTrace()));

        DialogHeaderPart header = new DialogHeaderPart();
        header.setHeaderText(title);
        this.addMessagePart(MessagePartConstants.DialogHeader, header);
        this.message = message;
        this.button = button;
        this.imageUrl = imageUrl;
        asDialog();
    }


}
