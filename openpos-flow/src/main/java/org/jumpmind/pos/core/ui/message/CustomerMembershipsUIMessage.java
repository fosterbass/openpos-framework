package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.AssignKeyBindings;
import org.jumpmind.pos.core.ui.UIMessage;

import java.util.List;

@Data
public class CustomerMembershipsUIMessage extends UIMessage {
    private static final long serialVersionUID = 1L;

    private List<UISubscriptionAccount> subscriptionAccounts;
    private ActionItem backButton;

    public CustomerMembershipsUIMessage() {
        setScreenType(UIMessageType.MEMBERSHIP_DETAILS_DIALOG);
    }
}
