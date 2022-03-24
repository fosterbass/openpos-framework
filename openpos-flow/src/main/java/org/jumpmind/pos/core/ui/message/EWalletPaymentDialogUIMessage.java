package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;

@Data
public class EWalletPaymentDialogUIMessage extends UIMessage {
    private String qrCodeUrl;
    private String message;
    private ActionItem cancelButton;

    public EWalletPaymentDialogUIMessage() {
        this.setScreenType(UIMessageType.E_WALLET_PAYMENT_DIALOG);
        this.asDialog();
    }
}
