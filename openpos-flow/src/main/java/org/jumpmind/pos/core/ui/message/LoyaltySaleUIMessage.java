package org.jumpmind.pos.core.ui.message;

import java.util.List;
import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;

@Data
public class LoyaltySaleUIMessage extends TransactionUIMessage {

    private static final long serialVersionUID = 1L;

    private UICustomer customer;
    private ActionItem loyaltyButton;
    private String loyaltyIDLabel;
    private String loyaltyIcon;
    private String profileIcon;

    private boolean customerMissingInfoEnabled;
    private boolean customerMissingInfo;
    private String customerMissingInfoIcon;
    private String customerMissingInfoLabel;
    private String customerEmail;

    private String checkMarkIcon;
    private String uncheckMarkIcon;

    private List<UIMembership> memberships;
    private boolean membershipEnabled;
    private String noMembershipsFoundLabel;
    private String memberIcon;
    private String nonMemberIcon;
    private String memberTierLabel;
    private String memberTier;

    private ActionItem mobileLoyaltyButton;
    private boolean mobileLoyaltySaleShowMembershipsHideLogo;

    private String loyaltySignupInProgressTitle;
    private String loyaltySignupInProgressIcon;
    private String loyaltySignupInProgressDetailsIcon;
    private ActionItem loyaltyCancelButton;

    private boolean rewardsVisibleOnLinkButton;
    private boolean membershipVisibleOnLinkButton;

    private String rewardsLabel;
    private String noPromotionsLabel;
    private List<UILoyaltyReward> loyaltyRewards;
}
