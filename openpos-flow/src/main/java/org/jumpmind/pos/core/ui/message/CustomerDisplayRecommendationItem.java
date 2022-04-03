package org.jumpmind.pos.core.ui.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerDisplayRecommendationItem implements Serializable {
    private String itemId;
    private String imageUrl;
    private String itemDescription;
}
