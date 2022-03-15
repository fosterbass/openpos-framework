package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.UIMessage;

@Data
public class CrestEnterUIMessage extends UIMessage {

    private static final long serialVersionUID = 1L;

    boolean crestOnlineFlag;
    String offlineImageUrl;
    String backgroundImage;

    public CrestEnterUIMessage() {
        setId("CrestEnter");
        setScreenType("CrestEnter");
        offlineImageUrl = "${apiServerBaseUrl}/crest/offline-dino.png";
    }
}

