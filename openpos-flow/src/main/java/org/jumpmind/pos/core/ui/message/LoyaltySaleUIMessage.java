package org.jumpmind.pos.core.ui.message;

import java.util.List;
import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;

@Data
public class LoyaltySaleUIMessage extends UIMessage {

    private static final long serialVersionUID = 1L;

    private ActionItem loyaltyButton;
    private String loyaltyIDLabel;
    private String profileIcon;
    private List<UIMembership> memberships;
    private boolean membershipEnabled;
    private boolean customerMissingInfoEnabled;
    private boolean customerMissingInfo;
    private String customerMissingInfoIcon;
    private String customerMissingInfoLabel;
    private String checkMarkIcon;
    private String uncheckMarkIcon;
    private String noMembershipsFoundLabel;
    private ActionItem mobileLoyaltyButton;

    private String loyaltyOperationInProgressTitle;
    private String loyaltyOperationInProgressIcon;
    private String loyaltyOperationInProgressDetailsIcon;
    private ActionItem loyaltyCancelButton;

    private boolean rewardsVisibleOnLinkButton;
    private boolean membershipVisibleOnLinkButton;
    private String customerEmail;
    private String memberTierLabel;
    private String rewardsLabel;
    private String memberTier;
    private String noPromotionsLabel;
    private String loyaltyIcon;
    private List<UILoyaltyReward> loyaltyRewards;
}
