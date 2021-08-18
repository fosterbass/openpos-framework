package org.jumpmind.pos.core.ui.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.core.ui.ActionItem;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UILoyaltyReward implements Serializable {
    private static final long serialVersionUID = 1L;

    private String promotionId;
    private String name;
    private String expirationDate;
    private String expirationLabel;
    private BigDecimal amount;
    private ActionItem applyButton;
}
