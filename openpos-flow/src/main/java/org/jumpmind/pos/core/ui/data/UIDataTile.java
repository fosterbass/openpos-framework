package org.jumpmind.pos.core.ui.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class UIDataTile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tileId;
    private String title;
    private String description;
    private String size;
    private UIGraph graph;
    private UIDataTable table;

    public UIDataTile() {
        this.setSize(UIDataTileSize.MEDIUM);
    }
}
