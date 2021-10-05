package org.jumpmind.pos.core.service;

import org.jumpmind.pos.core.flow.ApplicationState;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.core.flow.IStateManagerContainer;
import org.jumpmind.pos.core.flow.StateContext;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DeviceParamModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.server.service.IMessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DevToolsActionListenerTest {

    @Mock
    IStateManagerContainer stateManagerFactory;

    @Mock
    IMessageService messageService;

    @Mock
    DevicesRepository devicesRepository;

    @Mock
    IStateManager stateManager;

    @Mock
    ApplicationState applicationState;

    @Mock
    StateContext stateContext;

    @InjectMocks
    DevToolsActionListener devToolsActionListener;

    private final String mockDeviceId = "00000-001";

    @Before
    public void beforeTests() {
        devToolsActionListener.customerDisplayAppId = "customerdisplay";
        when(stateManagerFactory.retrieve(anyString(), anyBoolean())).thenReturn(stateManager);
        when(stateManager.getApplicationState()).thenReturn(applicationState);
        when(applicationState.getCurrentContext()).thenReturn(stateContext);
    }

    @Test
    public void testCustomerDisplayGeneration() {
        when(devicesRepository.getDevice(mockDeviceId)).thenReturn(getDevice(mockDeviceId));
        when(devicesRepository.getDevice(mockDeviceId + "-sim")).thenReturn(getDevice(mockDeviceId));
        devToolsActionListener.actionOccurred(mockDeviceId, new Action("fakeAction"));
        DeviceModel customerDisplay = getDevice(mockDeviceId + "-customerdisplay");
        customerDisplay.setAppId("customerdisplay");
        verify(devicesRepository, times(3)).getDevice(anyString());
        verify(devicesRepository).getDeviceAuth(anyString());
        verify(devicesRepository).saveDevice(customerDisplay);
        verify(devicesRepository).saveDeviceAuth(anyString(), anyString());
    }

    private DeviceModel getDevice(String deviceId) {
        DeviceParamModel deviceType = DeviceParamModel.builder()
                .paramName("deviceType")
                .paramValue("default")
                .build();
        return DeviceModel.builder()
                .deviceId(deviceId)
                .appId("pos")
                .deviceParamModels(new ArrayList<DeviceParamModel>() {{ add(deviceType); }})
                .build();
    }
}
