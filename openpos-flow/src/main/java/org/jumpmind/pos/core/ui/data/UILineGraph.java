package org.jumpmind.pos.core.ui.data;

import lombok.Data;
import org.jumpmind.pos.core.ui.data.UIGraph;
import org.jumpmind.pos.core.ui.data.UIGraphMultiDataPoint;
import org.jumpmind.pos.core.ui.data.UIGraphType;

import java.util.List;

@Data
public class UILineGraph extends UIGraph {

    private List<UIGraphMultiDataPoint> graphData;
    private boolean timelineEnabled;

    public UILineGraph() {
        this.setGraphType(UIGraphType.LINE);
    }
}
