package org.jumpmind.pos.core.ui.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class UIGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    private String graphType;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private boolean legendEnabled;
    private String legendTitle;
    private String legendPosition;
}
