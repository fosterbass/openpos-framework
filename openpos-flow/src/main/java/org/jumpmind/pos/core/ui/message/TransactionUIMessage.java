package org.jumpmind.pos.core.ui.message;

import lombok.Data;
import org.jumpmind.pos.core.model.Total;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.UIMessage;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionUIMessage extends UIMessage {
    private static final long serialVersionUID = 1L;

    private String providerKey;
    private boolean transactionActive = false;

    protected List<Total> totals;
    protected Total grandTotal;
    private List<Total> itemCounts;
    private boolean enableCollapsibleItems = true;

    private ActionItem checkoutButton;
    private ActionItem linkedEmployeeButton;
    private UICustomer employee;

    private boolean locationEnabled;
    private String locationOverridePrompt;

    public void addTotal(String name, String amount) {
        if (totals == null) {
            totals = new ArrayList<>();
        }
        totals.add(new Total(name, amount));
    }

    public void addItemCount(Total total) {
        if (itemCounts == null) {
            itemCounts = new ArrayList<>();
        }
        itemCounts.add(total);
    }

    public void addItemCount(String name, String amount) {
        addItemCount(new Total(name, amount));
    }

    public void setGrandTotal(String name, String amount) {
        this.grandTotal = new Total(name, amount);
    }
}
