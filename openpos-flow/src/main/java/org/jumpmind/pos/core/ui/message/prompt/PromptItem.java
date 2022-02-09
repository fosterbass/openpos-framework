package org.jumpmind.pos.core.ui.message.prompt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public abstract class PromptItem implements Serializable {
    private String id;
    private String type;
}
