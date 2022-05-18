package org.jumpmind.pos.core.ui.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPromotions implements Serializable {
    private String promotionsTitle;
    private String icon;
    private String noPromotionsLabel;
    private String promotionStackingDisclaimer;
    private List<DataTableRow> promotionDetails;
}
