package org.jumpmind.pos.core.ui.messagepart;

import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.IHasBackButton;

import java.io.Serializable;

@Data
public class SelfCheckoutMenuPart implements IHasBackButton, Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String operatorText;
    private String headerText;
    private String headerIcon;
    private ActionItem backButton;
    private ActionItem skipButton = new ActionItem("key:selfcheckout:button.skip", "Skip", false);
    private boolean showScan;
    private boolean showAdmin;
    private boolean showLanguageSelector = false;
    private String logo;

    public SelfCheckoutMenuPart() {
        logo = "content:home-screen-logo";
    }
}
