package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.ui.ActionItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class UICustomerDetailsItem extends SelectableItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String privacyRestrictedMessage;
    private String name;
    private String loyaltyNumber;
    private String accountNumberLabel;
    private String accountNumber;
    private String creditLimitLabel;
    private String creditLimit;
    private String expiryDateLabel;
    private String expiryDate;
    private BigDecimal loyaltyPoints;
    private String email;
    private String emailType;
    private String phoneNumber;
    private String phoneNumberType;
    private UIAddress address;
    private String birthDate;
    private String memberTier;
    private int numberOfActiveRewards;
    private int numberOfHistoricRewards;
    private List<UIMembership> memberships = new ArrayList<>();
    private ActionItem membershipSignUpAction;
    private ActionItem enrolledMembershipAction;
    private List<UILoyaltyReward> rewards = new ArrayList<>();
    private List<UIRewardHistory> rewardHistory = new ArrayList<>();
}
