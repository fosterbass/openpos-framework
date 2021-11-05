package org.jumpmind.pos.core.ui.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.core.ui.ActionItem;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UISubscriptionAccount implements Serializable {
    private String customerProgramId;
    private String iconImageUrl;
    private String iconText;
    private String copy;
    private String listTitle;
    private List<UISubscriptionPlan> plans;
    private List<UIEnrollmentItem> enrollmentItems;
    private ActionItem signupActionItem;
}
