package org.jumpmind.pos.core.ui.data;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Tab implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tabId;
    private String label;
    private String icon;
}
