package org.jumpmind.pos.core.ui.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class UICustomerDetailsItem extends SelectableItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String privacyRestricted;
    private String name;
    private String loyaltyNumber;
    private BigDecimal loyaltyPoints;
    private String email;
    private String emailType;
    private String phoneNumber;
    private String phoneNumberType;
    private UIAddress address;
    private int numberOfActiveRewards;
    private int numberOfHistoricRewards;
    private List<UIMembership> memberships = new ArrayList<>();
}
