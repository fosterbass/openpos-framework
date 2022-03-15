package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.flow.ActionHandler;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.AssignKeyBindings;
import org.jumpmind.pos.core.ui.NotificationItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.server.model.Action;

import java.util.ArrayList;
import java.util.List;

@Data
public class CrestHomeUIMessage extends UIMessage {

    private static final long serialVersionUID = 1L;

    boolean crestOnlineFlag;
    String offlineImageUrl;
    String crestURL;
    String backgroundImage;

    public CrestHomeUIMessage() {
        setId("CrestHome");
        setScreenType("CrestHome");
        offlineImageUrl = "${apiServerBaseUrl}/crest/offline-dino.png";
    }
}

