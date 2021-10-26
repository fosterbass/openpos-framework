package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.UIMessage;

@Data
public class StandbyUIMessage extends UIMessage {
    private static final long serialVersionUID = 1L;

    private String backgroundImage;

    public StandbyUIMessage() {
        setId("standby");
        setScreenType(UIMessageType.STANDBY);
        setShowStatusBar(false);
    }
}
