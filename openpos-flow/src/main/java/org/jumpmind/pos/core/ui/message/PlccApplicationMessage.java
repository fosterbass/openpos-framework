package org.jumpmind.pos.core.ui.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.util.model.Message;


import static org.jumpmind.pos.core.model.MessageType.PlccApply;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlccApplicationMessage extends Message {
    private static final long serialVersionUID = 1L;

    @JsonProperty("isActiveOnCustomerDisplay")
    private boolean isActiveOnCustomerDisplay;
    private String detailsMessage;

    public PlccApplicationMessage(boolean isActiveOnCustomerDisplay) {
        this.isActiveOnCustomerDisplay = isActiveOnCustomerDisplay;
    }

    @Override
    public String getType() {
        return PlccApply;
    }
}
