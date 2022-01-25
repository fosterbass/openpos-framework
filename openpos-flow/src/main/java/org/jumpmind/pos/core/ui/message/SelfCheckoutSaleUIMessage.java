package org.jumpmind.pos.core.ui.message;

public class SelfCheckoutSaleUIMessage extends SaleUIMessage {

    private static final long serialVersionUID = 1L;

    public SelfCheckoutSaleUIMessage() {
        this.setScreenType(UIMessageType.SELF_CHECKOUT_SALE);
        this.setId("selfcheckout-sale");
    }
}
