package org.jumpmind.pos.core.ui.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jumpmind.pos.core.model.FieldInputType;

@Data
public class PromptWithInfoScanAndExpiryDateUIMessage extends PromptWithInfoScanGiftCardUIMessage {
    
    @JsonProperty("isExpiryDateEnabled")
    private boolean isExpiryDateEnabled;
    @JsonProperty("hintDateText")
    private String hintDateText;
    @JsonProperty("placeholderDateText")
    private String placeholderDateText;
    private FieldInputType responseDateType;
    
    public PromptWithInfoScanAndExpiryDateUIMessage() {this.setScreenType("PromptWithInfoScanGiftCard");}
    
}
