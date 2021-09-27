package org.jumpmind.pos.core.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftReceiptUnmaskCodeDialogUIMessage extends UIMessage {
    public static final String GIFT_RECEIPT_UNMASK_CODE_WINDOW_ID = "GiftCodeChecker";

    private String closeText;
    private String closeAction;
    private String enterCodeLabel;
    private String enterGiftCode;
    private String showValue;
    private String resultLabel;
    private String resultValue;
    private String currencySymbol;
    private String incorrectResultValue;
    private String unmaskCode;

    @Override
    public String getScreenType() {
        return GIFT_RECEIPT_UNMASK_CODE_WINDOW_ID;
    }

    @Override
    public String getId() {
        return GIFT_RECEIPT_UNMASK_CODE_WINDOW_ID;
    }
}
