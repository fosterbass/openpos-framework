package org.jumpmind.pos.core.service;

import static org.jumpmind.pos.server.config.MessageUtils.COMPATIBILITY_VERSION_HEADER;

import static org.junit.Assert.assertEquals;

import org.jumpmind.pos.core.flow.ApplicationState;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.core.flow.IStateManagerContainer;
import org.jumpmind.pos.core.flow.Scope;
import org.jumpmind.pos.core.ui.message.DialogUIMessage;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.service.IDevicesService;
import org.jumpmind.pos.devices.service.model.GetChildDevicesResponse;
import org.jumpmind.pos.server.config.SessionSubscribedEvent;
import org.jumpmind.pos.server.service.IMessageService;
import org.jumpmind.pos.server.service.SessionConnectListener;
import org.jumpmind.pos.util.event.DeviceConnectedEvent;
import org.jumpmind.pos.util.event.EventPublisher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

public class SessionSubscribedListenerTest {

    @Mock
    private IStateManagerContainer stateManagerContainer;

    @Mock
    private IMessageService messageService;

    @Mock
    private SessionConnectListener sessionAuthTracker;

    @Mock
    private IDevicesService devicesService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private SessionSubscribedListener sessionSubscribedListener = new SessionSubscribedListener();

    @Captor
    private ArgumentCaptor<DialogUIMessage> dialogMessageCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> queryParamsCaptor;

    @Captor
    private ArgumentCaptor<Map<String, String>> personalizationParamsCaptor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private SessionSubscribedEvent makeMockEvent(
            String sessionId,
            String device,
            String compatibilityVersion) {
        SessionSubscribedEvent mockEvent = Mockito.mock(SessionSubscribedEvent.class);
        Message mockMessage = Mockito.mock(Message.class);
        MessageHeaders mockHeaders = Mockito.mock(MessageHeaders.class);

        Mockito.when(mockEvent.getMessage()).thenReturn(mockMessage);
        Mockito.when(mockMessage.getHeaders()).thenReturn(mockHeaders);

        Mockito.when(mockHeaders.get("simpSessionId")).thenReturn(sessionId);
        Mockito.when(mockHeaders.get("simpDestination")).thenReturn("/app/device/" + device);

        Map<String, List<String>> nativeHeaders = new HashMap<>();
        nativeHeaders.put(COMPATIBILITY_VERSION_HEADER, Arrays.asList(compatibilityVersion));

        Mockito.when(mockHeaders.get("nativeHeaders")).thenReturn(nativeHeaders);
        return mockEvent;
    }

    private IStateManager makeMockStateManager() {
        IStateManager mockStateManager = Mockito.mock(IStateManager.class);
        ApplicationState applicationState = Mockito.mock(ApplicationState.class);
        Scope scope = Mockito.mock(Scope.class);

        Mockito.when(mockStateManager.getApplicationState()).thenReturn(applicationState);
        Mockito.when(applicationState.getScope()).thenReturn(scope);

        return mockStateManager;
    }

    @Test
    public void onApplicationEventIfSessionIsNotAuthenticatedShowErrorDialog() {
        Mockito.when(sessionAuthTracker.isSessionAuthenticated(Mockito.any())).thenReturn(false);

        sessionSubscribedListener.onApplicationEvent(
                makeMockEvent(
                        "123456",
                        "11111-111",
                        "1"));

        Mockito.verify(messageService).sendMessage(
                Mockito.matches("11111-111"),
                dialogMessageCaptor.capture());

        assertEquals("Failed Authentication", dialogMessageCaptor.getValue().getDialogHeaderPart().getHeaderText());
    }

    @Test
    public void onApplicationEventIfSessionNotCompatibleShowErrorDialog() {
        Mockito.when(sessionAuthTracker.isSessionAuthenticated(Mockito.any())).thenReturn(true);
        Mockito.when(sessionAuthTracker.isSessionCompatible(Mockito.any())).thenReturn(false);

        ReflectionTestUtils.setField(sessionSubscribedListener, "incompatibleVersionMessage", "Not compatible");

        sessionSubscribedListener.onApplicationEvent(
                makeMockEvent(
                        "123456",
                        "11111-111",
                        "1"));

        Mockito.verify(messageService).sendMessage(
                Mockito.matches("11111-111"),
                dialogMessageCaptor.capture());

        assertEquals("Incompatible Versions", dialogMessageCaptor.getValue().getDialogHeaderPart().getHeaderText());
    }

    @Test
    public void onApplicationEventCreateNewStateManager() {
        Mockito.when(sessionAuthTracker.isSessionAuthenticated(Mockito.any())).thenReturn(true);
        Mockito.when(sessionAuthTracker.isSessionCompatible(Mockito.any())).thenReturn(true);
        Mockito.when(sessionAuthTracker.getDeviceModel(Mockito.anyString())).thenReturn(DeviceModel.builder().deviceId("11111-111").appId("pos").build());
        Mockito.when(stateManagerContainer.retrieve(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(null);
        IStateManager stateManager = makeMockStateManager();
        Mockito.when(stateManagerContainer.create(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(stateManager);

        sessionSubscribedListener.onApplicationEvent(
                makeMockEvent(
                        "123456",
                        "11111-111",
                        "1"));

        Mockito.verify(stateManagerContainer).create(
                Mockito.matches("pos"),
                Mockito.matches("11111-111"),
                queryParamsCaptor.capture(),
                personalizationParamsCaptor.capture());

        Mockito.verify(stateManagerContainer).setCurrentStateManager(stateManager);
    }

    @Test
    public void onApplicationEventRetrieveStateManagerAndUpdate() {
        Mockito.when(sessionAuthTracker.isSessionAuthenticated(Mockito.any())).thenReturn(true);
        Mockito.when(sessionAuthTracker.isSessionCompatible(Mockito.any())).thenReturn(true);
        Mockito.when(sessionAuthTracker.getDeviceModel(Mockito.any())).thenReturn(DeviceModel.builder()
                        .appId("pos")
                        .deviceId("11111-111")
                        .build()
        );
        Mockito.when(devicesService.getChildDevices(Mockito.any())).thenReturn(new GetChildDevicesResponse());

        IStateManager stateManager = makeMockStateManager();
        Mockito.when(stateManagerContainer.retrieve(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(stateManager);

        sessionSubscribedListener.onApplicationEvent(
                makeMockEvent(
                        "123456",
                        "11111-111",
                        "1"));

        Mockito.verify(stateManagerContainer, Mockito.never()).create(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(stateManagerContainer).setCurrentStateManager(stateManager);
        Mockito.verify(stateManager).registerQueryParams(Mockito.any());
        Mockito.verify(stateManager).registerPersonalizationProperties(Mockito.any());
        Mockito.verify(stateManager).sendConfigurationChangedMessage();
        Mockito.verify(eventPublisher).publish(Mockito.isA(DeviceConnectedEvent.class));
        Mockito.verify(stateManager).refreshScreen();
    }
}
