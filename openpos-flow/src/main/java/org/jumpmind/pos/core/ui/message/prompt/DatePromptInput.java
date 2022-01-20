package org.jumpmind.pos.core.ui.message.prompt;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DatePromptInput extends BaseInputPrompt<Date> {
    private DateFormat format;
    private Date min;
    private Date max;

    @Builder
    protected DatePromptInput(
            String id,
            String label,
            DateFormat format,
            Date defaultValue,
            boolean required,
            Date min,
            Date max,
            String hint
    ) {
        super(id, PromptItemTypes.DATE, label, defaultValue, required, hint);

        this.format = format;
        this.min = min;
        this.max = max;
    }
}
