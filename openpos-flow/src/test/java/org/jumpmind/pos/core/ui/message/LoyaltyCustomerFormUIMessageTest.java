package org.jumpmind.pos.core.ui.message;

import org.jumpmind.pos.core.ui.ActionItem;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoyaltyCustomerFormUIMessageTest {

    @Test
    public void createLoyaltyCustomerFormUIMessageWithAllProperties() {
        LoyaltyCustomerFormUIMessage screen = new LoyaltyCustomerFormUIMessage();
        assertEquals("LoyaltyCustomerDialog", screen.getScreenType());
        assertNotNull(screen.getForm());
        assertNotNull(screen.getAlternateSubmitActions());
        screen.setIsStructuredForm(true);
        screen.setMembershipEnabled(true);
        screen.setMemberships(new ArrayList<>());
        screen.setMembershipsLabel("Label");
        screen.setNoMembershipsLabel("Label");
        screen.setAddPhone(new ActionItem("Action"));
        screen.setRemovePhone(new ActionItem("Action"));
        screen.setAddEmail(new ActionItem("Action"));
        screen.setRemoveEmail(new ActionItem("Action"));
        screen.setCountrySelected(new ActionItem("Action"));
        screen.setStateSelected(new ActionItem("Action"));
        screen.setSubmitButton(new ActionItem("Action"));
        screen.setSecondaryButton(new ActionItem("Action"));
        screen.setProfileIcon("Icon");
        screen.setLoyaltyNumberIcon("Icon");
        screen.setPhoneIcon("Icon");
        screen.setEmailIcon("Icon");
        screen.setLocationIcon("Icon");
        screen.setMembershipsIcon("Icon");
        screen.setAddIcon("Icon");
        screen.setRemoveIcon("Icon");
        screen.setCheckMarkIcon("Icon");
        screen.setInstructions("Instruction");
        screen.setImageUrl("URL");
    }
}
