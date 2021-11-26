package org.jumpmind.pos.core.ui.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PromptWithInfoScanGiftCardUIMessage extends PromptWithInfoUIMessage {
    
    @JsonProperty("isRequiredInputField")
    private boolean isRequiredInputField;
    @JsonProperty("isGiftCardScanEnabled")
    private boolean isGiftCardScanEnabled;

    public boolean isRequiredInputField() {
        return isRequiredInputField;
    }

    public void setRequiredInputField(boolean requiredInputField) {
        isRequiredInputField = requiredInputField;
    }

    public boolean isScanGiftCardEnabled() {
        return isGiftCardScanEnabled;
    }

    public void setScanGiftCardEnabled(boolean scanEnabled) {
        isGiftCardScanEnabled = scanEnabled;
    }

    
    public PromptWithInfoScanGiftCardUIMessage(boolean isRequiredInputField, boolean isScanGiftCardEnabled) {
        this.isRequiredInputField = isRequiredInputField;
        this.isGiftCardScanEnabled = isScanGiftCardEnabled;
        this.setAutoFocus(false);
    }
    
    public PromptWithInfoScanGiftCardUIMessage() {
        this.setAutoFocus(false);
    }
    
    
}
