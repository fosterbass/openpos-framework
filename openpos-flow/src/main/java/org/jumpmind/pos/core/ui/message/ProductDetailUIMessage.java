package org.jumpmind.pos.core.ui.message;

import lombok.*;
import org.jumpmind.pos.core.model.DisplayProperty;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.data.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProductDetailUIMessage extends UIMessage {
    private String productName;
    private List<DisplayProperty> productIdentifiers;
    private String price;
    private String availabilityLabel;
    private String availabilityType;
    private List<String> imageUrls;
    private String alternateImageUrl;
    private List<ProductOptionComponent> productOptionsComponents;
    private List<Tab> tabs;
    private List<TabContent> tabContents;
    private ProductPromotions productPromotions;
    private ProductInventory productInventory;
    private List<ActionItem> actions;

    @Override
    public String getScreenType() {
        return UIMessageType.PRODUCT_DETAIL;
    }

    public void addProductIdentifier(DisplayProperty displayProperty) {
        if (productIdentifiers == null) {
            productIdentifiers = new ArrayList<>();
        }
        productIdentifiers.add(displayProperty);
    }

    public void addProductOptionComponent(String componentType, String componentName){
        if(productOptionsComponents == null){
            productOptionsComponents = new ArrayList<>();
        }
        productOptionsComponents.add(ProductOptionComponent.builder().name(componentName).type(componentType).build());
    }
}
