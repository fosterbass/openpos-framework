package org.jumpmind.pos.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.core.flow.IStateManagerContainer;
import org.jumpmind.pos.core.flow.StateManager;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.service.IDevicesService;
import org.jumpmind.pos.devices.service.model.DisconnectDeviceRequest;
import org.jumpmind.pos.server.service.SessionConnectListener;
import org.jumpmind.pos.util.event.DeviceDisconnectedEvent;
import org.jumpmind.pos.util.event.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class SessionDisconnectedListener implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    IStateManagerContainer stateManagerContainer;

    @Autowired
    SessionConnectListener sessionConnectListener;

    @Autowired
    SessionSubscribedListener sessionSubscribedListener;

    @Autowired
    EventPublisher eventPublisher;

    @Autowired
    IDevicesService devicesService;
    
    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        synchronized (sessionSubscribedListener) {
            Message<?> msg = event.getMessage();
            String sessionId = (String) msg.getHeaders().get("simpSessionId");
            log.info("session disconnected: {}", sessionId);
            DeviceModel deviceModel = sessionConnectListener.getDeviceModel(sessionId);
            if (deviceModel != null) {
                devicesService.disconnectDevice(new DisconnectDeviceRequest(deviceModel.getDeviceId()));
                try {
                    eventPublisher.publish(new DeviceDisconnectedEvent(deviceModel.getDeviceId(), deviceModel.getAppId(), deviceModel.getPairedDeviceId()));
                } catch (Exception ex) {
                    log.warn("Error publishing DeviceDisconnectedEvent", ex);
                }

                // todo: comparison of the app id is temporary while we evaluate other solutions
                if (StringUtils.equals(deviceModel.getAppId(), "customerdisplay")) {
                    stateManagerContainer.remove(deviceModel.getDeviceId());
                } else {
                    IStateManager stateManager = stateManagerContainer.retrieve(deviceModel.getDeviceId(), false);
                    if (stateManager != null) {
                        stateManager.setConnected(false);
                    }
                }

                SubscribedSessionMetric.dec(deviceModel.getDeviceId());
            } else {
                log.warn("No device found for session id=" + sessionId + ", not publishing DeviceDisconnectedEvent.");
            }
            sessionConnectListener.removeSession(sessionId);
            stateManagerContainer.setCurrentStateManager(null);
        }
    }

}