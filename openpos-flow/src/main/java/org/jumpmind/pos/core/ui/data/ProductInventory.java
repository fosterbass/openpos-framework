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
public class ProductInventory implements Serializable {
    private String inventoryTitle;
    private String icon;
    private List<InventoryDetail> businessUnitInventoryDetails;
    private String noBuddyStoresMessage;
    private String buddyStoreOfflineMessage;
    private String buddyStoreProviderKey;
}
