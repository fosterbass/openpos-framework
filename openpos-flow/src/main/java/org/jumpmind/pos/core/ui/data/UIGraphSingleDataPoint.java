package org.jumpmind.pos.core.ui.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class UIGraphSingleDataPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private Object xValue;
    private Object yValue;
}
