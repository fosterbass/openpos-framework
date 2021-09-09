package org.jumpmind.pos.core.ui.data;

import lombok.Data;

import java.util.List;

@Data
public class UIPieChart extends UIGraph {

    private List<UIGraphSingleDataPoint> graphData;
    private String pieChartType;

    public UIPieChart() {
        this.setGraphType(UIGraphType.PIE_CHART);
        this.setPieChartType(UIPieChartType.CLASSIC);
    }
}
