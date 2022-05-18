package org.jumpmind.pos.core.ui.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductInventory implements Serializable {
    private String inventoryTitle;
    private String icon;
    private String onHandLabel;
    private int onHandCount;
    private String reservedLabel;
    private int reservedCount;
    private String damagedLabel;
    private int damagedCount;
    private String noBuddyStoresMessage;
    private String buddyStoreOfflineMessage;
    private String inventoryMessageProviderKey;
    private String buddyStoreProviderKey;
}
