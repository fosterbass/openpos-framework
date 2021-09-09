package org.jumpmind.pos.core.ui.data;

import lombok.Data;

import java.util.List;

@Data
public class UIBarGraph extends UIGraph {

    private List<UIGraphSingleDataPoint> graphData;

    public UIBarGraph() {
        this.setGraphType(UIGraphType.BAR);
    }
}
