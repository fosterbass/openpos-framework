package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.messagepart.SelfCheckoutMenuPart;
import org.jumpmind.pos.core.ui.messagepart.OptionsListPart;

@Data
public class SelfCheckoutOptionsUIMessage extends UIMessage {

    private static final long serialVersionUID = 1L;

    private SelfCheckoutMenuPart selfCheckoutMenu = new SelfCheckoutMenuPart();

    private String title;

    private String prompt;

    private OptionsListPart optionsList;

    private String imageUrl;

    private String icon;

    public SelfCheckoutOptionsUIMessage() {
        setScreenType(UIMessageType.SELF_CHECKOUT_OPTIONS);
    }

}
