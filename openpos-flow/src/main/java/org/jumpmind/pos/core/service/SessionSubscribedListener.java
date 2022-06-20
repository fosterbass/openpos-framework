package org.jumpmind.pos.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.core.flow.IStateManagerContainer;
import org.jumpmind.pos.core.flow.ScopeType;
import org.jumpmind.pos.core.ui.DialogProperties;
import org.jumpmind.pos.core.ui.IconType;
import org.jumpmind.pos.core.ui.message.DialogUIMessage;
import org.jumpmind.pos.core.ui.messagepart.DialogHeaderPart;
import org.jumpmind.pos.core.ui.messagepart.MessagePartConstants;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.service.IDevicesService;
import org.jumpmind.pos.devices.service.model.GetChildDevicesRequest;
import org.jumpmind.pos.devices.service.model.GetDeviceRequest;
import org.jumpmind.pos.server.config.MessageUtils;
import org.jumpmind.pos.server.config.SessionSubscribedEvent;
import org.jumpmind.pos.server.service.IMessageService;
import org.jumpmind.pos.server.service.SessionConnectListener;
import org.jumpmind.pos.util.Version;
import org.jumpmind.pos.util.event.DeviceConnectedEvent;
import org.jumpmind.pos.util.event.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.jumpmind.pos.util.AppUtils.setupLogging;

@Component
@Slf4j
public class SessionSubscribedListener implements ApplicationListener<SessionSubscribedEvent>, MessageUtils {

    @Autowired
    private IStateManagerContainer stateManagerContainer;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private SessionConnectListener sessionAuthTracker;

    @Value("${openpos.incompatible.version.message:The compatibility version of the client does not match the server}")
    private String incompatibleVersionMessage;

    @Autowired
    EventPublisher eventPublisher;

    @Autowired
    IDevicesService devicesService;


    @SuppressWarnings("unchecked")
    @Override
    public synchronized void onApplicationEvent(SessionSubscribedEvent event) {
        Message<?> msg = event.getMessage();
        String sessionId = (String) msg.getHeaders().get("simpSessionId");
        Map<String, Object> queryParams = sessionAuthTracker.getQueryParams(sessionId);
        Map<String, String> deviceVariables = sessionAuthTracker.getDeviceVariables(sessionId);
        String topicName = (String) msg.getHeaders().get("simpDestination");
        String compatibilityVersion = this.getHeader(msg, MessageUtils.COMPATIBILITY_VERSION_HEADER);
        String deviceId = StringUtils.isNotBlank(topicName) ? topicName.substring(topicName.indexOf("/device/") + "/device/".length()) : "";
        setupLogging(deviceId);
        Map<String, String> personalizationProperties = sessionAuthTracker.getPersonalizationResults(sessionId);

        try {
            log.info("session {} subscribed to {}", sessionId, topicName);
            if (!sessionAuthTracker.isSessionAuthenticated(sessionId)) {
                DialogUIMessage errorDialog = new DialogUIMessage();
                DialogHeaderPart header = new DialogHeaderPart();
                errorDialog.asDialog(new DialogProperties(false));
                header.setHeaderIcon(IconType.Error);
                header.setHeaderText("Failed Authentication");
                errorDialog.addMessagePart(MessagePartConstants.DialogHeader, header);
                errorDialog.setMessage(Collections.singletonList("The client and server authentication tokens did not match"));
                messageService.sendMessage(deviceId, errorDialog);
                return;
            } else if (!sessionAuthTracker.isSessionCompatible(sessionId)) {
                log.warn("Client compatibility version of '{}' for deviceId '{}' is not compatible with the server", compatibilityVersion,
                        deviceId);
                DialogUIMessage errorDialog = new DialogUIMessage();
                // If there is no compatibility version, the client is an older
                // client that used the type attribute
                // instead of the screenType attribute for the screen type
                // value. In that case need to set the type attribute or
                // the dialog will not display on older clients
                if (compatibilityVersion == null) {
                    errorDialog.setType(errorDialog.getScreenType());
                }
                errorDialog.asDialog(new DialogProperties(false));
                DialogHeaderPart header = new DialogHeaderPart();
                header.setHeaderIcon(IconType.Error);
                header.setHeaderText("Incompatible Versions");
                errorDialog.addMessagePart(MessagePartConstants.DialogHeader, header);
                errorDialog.setMessage(Arrays.asList(incompatibleVersionMessage.split("\n")));
                messageService.sendMessage(deviceId, errorDialog);
                return;
            }

            IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
            boolean created = false;
            DeviceModel sessionDevice = sessionAuthTracker.getDeviceModel(sessionId);
            String appId = sessionDevice != null ? sessionDevice.getAppId() : null;
            if (stateManager == null) {
                // If your first state has a
                stateManager = stateManagerContainer.create(appId, deviceId, queryParams, personalizationProperties);
                created = true;
            } else {
                stateManager.registerQueryParams(queryParams);
                stateManager.registerPersonalizationProperties(personalizationProperties);
                stateManager.sendConfigurationChangedMessage();
                stateManager.sendStartupCompleteMessage();
            }

            stateManagerContainer.setCurrentStateManager(stateManager);

            stateManager.setDeviceVariables(deviceVariables);
            stateManager.setConnected(sessionAuthTracker.isSessionAuthenticated(sessionId));

            final DeviceModel myDevice = sessionAuthTracker.getDeviceModel(sessionId);

            stateManager.getApplicationState().getScope().setDeviceScope("device", myDevice);
            stateManager.getApplicationState().getScope().setDeviceScope("powerStatus", sessionAuthTracker.getPowerStatus(sessionId));

            if (StringUtils.isNotBlank(myDevice.getParentDeviceId())) {
                try {
                    final DeviceModel parentDevice = devicesService.getDevice(
                            GetDeviceRequest.builder().deviceId(myDevice.getParentDeviceId()).build()
                    ).getDeviceModel();

                    if (parentDevice != null) {
                        stateManager.getApplicationState().getScope().setDeviceScope("parentDevice", parentDevice);
                    }
                } catch (Exception ex) {
                    log.error("parent device was indicated by the connecting device '{}' but an unknown error occurred while attempting to locate the device; device pairing will be incomplete and non-functional...", myDevice.getDeviceId(), ex);
                }
            }

            try {
                final List<DeviceModel> children = devicesService.getChildDevices(GetChildDevicesRequest.builder().parentDeviceId(myDevice.getDeviceId()).build())
                        .getChildren();

                if (CollectionUtils.isNotEmpty(children)) {
                    List<DeviceModel> childDevices = stateManager.getApplicationState().getScopeValue(ScopeType.Device, "childDevices");
                    if (childDevices == null) {
                        childDevices = new ArrayList<>();
                    }

                    childDevices.addAll(children);
                    stateManager.getApplicationState().getScope().setDeviceScope("childDevices", childDevices);
                }
            } catch (Exception ex) {
                log.error("failed to determine if device '{}' has any assigned children; device pairing will be incomplete and non-functional...", myDevice.getDeviceId(), ex);
            }

            if (!created) {
                stateManager.refreshScreen();
            }

            eventPublisher.publish(new DeviceConnectedEvent(deviceId, appId, (List<Version>) queryParams.get("deviceVersions")));

            SubscribedSessionMetric.inc(deviceId);
        } catch (Exception ex) {
            log.error("Failed to subscribe to " + topicName, ex);
            DialogUIMessage errorDialog = new DialogUIMessage();
            errorDialog.asDialog(new DialogProperties(false));
            DialogHeaderPart header = new DialogHeaderPart();
            header.setHeaderIcon(IconType.Error);
            header.setHeaderText("Failed To Subscribe");
            errorDialog.addMessagePart(MessagePartConstants.DialogHeader, header);
            errorDialog.setMessage(Collections.singletonList(ex.getMessage()));
            messageService.sendMessage(deviceId, errorDialog);
        } finally {
            stateManagerContainer.setCurrentStateManager(null);
        }
    }

}
