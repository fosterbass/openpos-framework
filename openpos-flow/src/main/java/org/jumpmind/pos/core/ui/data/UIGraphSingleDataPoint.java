package org.jumpmind.pos.core.ui.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UIGraphSingleDataPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object xValue;
    private Object yValue;
}
