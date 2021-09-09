package org.jumpmind.pos.core.ui.data;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UIDataTable implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> columnHeaders = new ArrayList<String>();
    private List<DataTableRow> rows = new ArrayList<>();
}
