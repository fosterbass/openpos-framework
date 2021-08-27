package org.jumpmind.pos.core.ui.messagepart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int angleLower;
    private int angleUpper;
    @Builder.Default
    private int distance = 500;
}
