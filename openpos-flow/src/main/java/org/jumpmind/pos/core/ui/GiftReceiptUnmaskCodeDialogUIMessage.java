package org.jumpmind.pos.core.ui;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jumpmind.pos.core.ui.messagepart.PromptButtonRowPart;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftReceiptUnmaskCodeDialogUIMessage extends UIMessage {
    public static final String GIFT_RECEIPT_UNMASK_CODE_WINDOW_ID = "GiftCodeChecker";

    private String enterCodeLabel;
    private String enterGiftCode;
    private String showValue;
    private String resultLabel;
    private String resultValue;
    private String currencySymbol;
    private String incorrectResultValue;
    private String unmaskCode;
    private String placeholder;
    private boolean autofocus;
    private PromptButtonRowPart promptButtonRow;

    public void setButtons(List<ActionItem> buttons) {
        if (buttons != null && buttons.size() > 0) {
            promptButtonRow.setPrimaryButton(buttons.get(0));

            for (int b = 1; b < buttons.size(); b++) {
                promptButtonRow.addSecondaryButton(buttons.get(b));
            }
        }
    }

    public void addButton(ActionItem button) {
        if (promptButtonRow.getPrimaryButton() == null) {
            promptButtonRow.setPrimaryButton(button);
        } else {
            promptButtonRow.addSecondaryButton(button);
        }
    }

    @Override
    public String getScreenType() {
        return GIFT_RECEIPT_UNMASK_CODE_WINDOW_ID;
    }

    @Override
    public String getId() {
        return GIFT_RECEIPT_UNMASK_CODE_WINDOW_ID;
    }
}
