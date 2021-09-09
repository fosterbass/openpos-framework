package org.jumpmind.pos.core.ui.data;

import lombok.Data;

import java.util.List;

@Data
public class UIScatterPlotGraph extends UIGraph {

    private List<UIGraphMultiDataPoint> graphData;

    public UIScatterPlotGraph() {
        this.setGraphType(UIGraphType.SCATTER_PLOT);
    }
}
