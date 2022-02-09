package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.AssignKeyBindings;
import org.jumpmind.pos.core.ui.UIMessage;

import java.util.ArrayList;
import java.util.List;

@Data
@AssignKeyBindings
public class CustomerDetailsUIMessage extends UIMessage {
    private static final long serialVersionUID = 1L;

    private String title;
    private String itemsHistoryDataProviderKey;
    private String rewardsHistoryDataKey;
    private String rewardsDataKey;

    private ActionItem backButton;
    private List<ActionItem> secondaryButtons;
    private ActionItem doneButton;

    private UICustomerDetailsItem customer;

    private Boolean membershipEnabled;
    private Boolean membershipPointsEnabled;
    private Boolean rewardTabEnabled;
    private Boolean rewardHistoryTabEnabled;
    private Boolean itemHistoryEnabled;

    private String appliedLabel;
    private String membershipLabel;
    private String contactLabel;
    private String plccAccountDetailsLabel;
    private String noPromotionsLabel;
    private String rewardsLabel;
    private String expiresLabel;
    private String rewardHistoryIcon;
    private String rewardHistoryLabel;
    private String expiredLabel;
    private String loyaltyProgramNameLabel;
    private String pointsLabel;
    private String redeemedLabel;
    private String noMembershipsFoundLabel;
    private String profileIcon;
    private String membershipCardIcon;
    private String membershipPointsIcon;
    private String emailIcon;
    private String phoneIcon;
    private String loyaltyIcon;
    private String loyaltyNumberIcon;
    private String locationIcon;
    private String memberIcon;
    private String nonMemberIcon;
    private String checkMarkIcon;
    private String uncheckMarkIcon;
    private String expiredIcon;
    private String redeemedIcon;
    private String appliedIcon;
    private String applyIcon;
    private String backIcon;
    private String birthDateIcon;
    private String memberTierLabel;
    private String itemHistoryLabel;
    private String itemHistoryIcon;
    private String itemHistoryFilterLabel;

    private UICustomerItemHistoryFilter itemHistoryFilter;

    public CustomerDetailsUIMessage() {
        setScreenType(UIMessageType.CUSTOMER_DETAILS_DIALOG);
    }

    public void addSecondaryButtons(ActionItem action) {
        if (secondaryButtons == null) {
            secondaryButtons = new ArrayList<>();
        }
        secondaryButtons.add(action);
    }
}
