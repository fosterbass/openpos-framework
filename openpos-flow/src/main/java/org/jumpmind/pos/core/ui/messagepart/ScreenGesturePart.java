package org.jumpmind.pos.core.ui.messagepart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenGesturePart implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action;
    private List<String> swipes;
    private List<PanEvent> pans;
    private int swipeTimeout;
}
