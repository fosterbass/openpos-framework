package org.jumpmind.pos.core.ui.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.jumpmind.pos.core.model.MessageType;
import org.jumpmind.pos.util.model.Message;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltySignupMessage extends Message {
    private static final long serialVersionUID = 1L;

    @JsonProperty("isActiveOnCustomerDisplay")
    private boolean isActiveOnCustomerDisplay;
    private String detailsMessage;

    public LoyaltySignupMessage(boolean isActiveOnCustomerDisplay) {
        this.isActiveOnCustomerDisplay = isActiveOnCustomerDisplay;
    }

    @Override
    public String getType() {
        return MessageType.LoyaltySignup;
    }
}
