package org.jumpmind.pos.core.ui.message;

public class CustomerDisplayReturnUIMessage extends SaleUIMessage {
    private static final long serialVersionUID = 1L;

    public CustomerDisplayReturnUIMessage() {
        this.setScreenType(UIMessageType.CUSTOMER_DISPLAY_RETURN);
        this.setId("customerdisplay-return");
    }
}
