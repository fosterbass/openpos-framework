package org.jumpmind.pos.core.push;

import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.server.service.IMessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FirebasePushNotificationManagerTest {

    @Spy
    FirebasePushNotificationManager pushNotificationManager;

    @Mock
    IMessageService messageService;

    @InjectMocks
    PushNotificationActionListener actionListener;

    private static final String REG_TOKEN_ONE = "eVGrV3xhQ4CXDUMoZ6khq5:APA91bE45J2kzMe_Jy2FXp-LW2Uoud7mp3OHBRX7FhQWIao2xhe5NbaUpe-_xiQpotzb1N9IRkNwUWvja7iRjIX9zkh9FRztlmWHdnlSR0NY-nH4SuT83B9DKZnCRXUqDrjjyUq15e3Y";
    private static final String REG_TOKEN_TWO = "ALT_eVGrV3xhQ4CXDUMoZ6khq5:APA91bE45J2kzMe_Jy2FXp-LW2Uoud7mp3OHBRX7FhQWIao2xhe5NbaUpe-_xiQpotzb1N9IRkNwUWvja7iRjIX9zkh9FRztlmWHdnlSR0NY-nH4SuT83B9DKZnCRXUqDrjjyUq15e3Y";

    @Before
    public void beforeTests() {
        pushNotificationManager.messageService = messageService;
        actionListener.firebasePushNotificationManager = pushNotificationManager;
    }

    @Test
    public void testDeviceRegistration() {
        doAnswer(i -> {
            actionListener.actionOccurred("00000-001",new Action("PushNotification", REG_TOKEN_ONE));
            return null;
        }).when(messageService).sendMessage(anyString(), any());
        String token = pushNotificationManager.registerDeviceForNotifications("00000-001");
        verify(pushNotificationManager).saveDevice(REG_TOKEN_ONE);
        assertEquals(REG_TOKEN_ONE, token);
    }

    @Test
    public void testMultipleDeviceRegistration() {
        doAnswer(i -> {
            actionListener.actionOccurred("00000-001",new Action("PushNotification", REG_TOKEN_ONE));
            return null;
        }).when(messageService).sendMessage(anyString(), any());
        String token = pushNotificationManager.registerDeviceForNotifications("00000-001");

        doAnswer(i -> {
            actionListener.actionOccurred("00000-002",new Action("PushNotification", REG_TOKEN_TWO));
            return null;
        }).when(messageService).sendMessage(anyString(), any());
        String tokenAlt = pushNotificationManager.registerDeviceForNotifications("00000-002");
        verify(pushNotificationManager).saveDevice(REG_TOKEN_ONE);
        verify(pushNotificationManager).saveDevice(REG_TOKEN_TWO);
        assertEquals(REG_TOKEN_ONE, token);
        assertEquals(REG_TOKEN_TWO, tokenAlt);
    }


}