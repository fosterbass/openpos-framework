package org.jumpmind.pos.core.ui.data;

import lombok.Data;

import java.util.List;

@Data
public class UIPieChart extends UIGraph {

    private List<UIGraphSingleDataPoint> graphData;
    private String pieChartType;

    public UIPieChart() {
        this.setGraphType(UIGraphType.PIE);
        this.setPieChartType(UIPieChartType.CLASSIC);
    }
}
