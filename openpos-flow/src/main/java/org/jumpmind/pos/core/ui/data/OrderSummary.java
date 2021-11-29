package org.jumpmind.pos.core.ui.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummary implements Serializable {
    private String number;
    private String title;
    private OrderCustomer customer;
    private boolean priorityFlag;
    private String orderType;
    private String orderTypeLabel;
    private String orderTypeIcon;
    private String itemCount;
    private String itemCountLabel;
    private String itemCountIcon;
    private String statusCode;
    private String status;
    private String statusLabel;
    private String statusIcon;
    private Date orderDue;
    private String orderDueLabel;
    private String orderDueIcon;
    private Date orderCreated;
    private String orderCreatedLabel;
    private String orderTotal;
    private String orderTotalLabel;
    private String amountDue;
    private String amountDueLabel;
    private String paymentStatus;
    private TimeUnitLabels timeUnitLabels;
}


