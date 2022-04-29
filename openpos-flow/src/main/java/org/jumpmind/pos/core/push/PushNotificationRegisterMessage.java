package org.jumpmind.pos.core.push;

import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.message.UIMessageType;

public class PushNotificationRegisterMessage extends UIMessage {
    private static final long serialVersionUID = 1L;

    public PushNotificationRegisterMessage() {
        setScreenType(UIMessageType.PUSH_REGISTER);
    }

    @Override
    public String getType() {
        return UIMessageType.PUSH_REGISTER;
    }
}
