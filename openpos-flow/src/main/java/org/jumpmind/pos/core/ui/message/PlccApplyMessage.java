package org.jumpmind.pos.core.ui.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.jumpmind.pos.core.model.MessageType;
import org.jumpmind.pos.util.model.Message;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlccApplyMessage extends Message {
    private static final long serialVersionUID = 1L;

    @JsonProperty("isActiveOnCustomerDisplay")
    private boolean isActiveOnCustomerDisplay;
    private String detailsMessage;

    public PlccApplyMessage(boolean isActiveOnCustomerDisplay) {
        this.isActiveOnCustomerDisplay = isActiveOnCustomerDisplay;
    }

    @Override
    public String getType() {
        return MessageType.PlccApply;
    }
}
