package org.jumpmind.pos.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DeviceParamModel;
import org.jumpmind.pos.devices.service.IDevicesService;
import org.jumpmind.pos.devices.service.model.AuthenticateDeviceRequest;
import org.jumpmind.pos.devices.service.model.PersonalizationParameter;
import org.jumpmind.pos.devices.service.model.PersonalizationParameters;
import org.jumpmind.pos.server.config.MessageUtils;
import org.jumpmind.pos.util.BoxLogging;
import org.jumpmind.pos.util.DefaultObjectMapper;
import org.jumpmind.pos.util.Version;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.pos.util.clientcontext.ClientContextConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component("serverCoreSessionConnectListener")
public class SessionConnectListener implements ApplicationListener<SessionConnectEvent>, MessageUtils {

    Logger log = LoggerFactory.getLogger(getClass());

    Map<String, Boolean> sessionAuthenticated = Collections.synchronizedMap(new HashMap<>());

    Map<String, Boolean> sessionCompatible = Collections.synchronizedMap(new HashMap<>());

    Map<String, Map<String, String>> sessionPersonalizationResults = Collections.synchronizedMap(new HashMap<>());

    Map<String, Map<String, Object>> sessionQueryParamsMap = Collections.synchronizedMap(new HashMap<>());

    Map<String, String> sessionAppIdMap = Collections.synchronizedMap(new HashMap<>());

    Map<String, Map<String, String>> deviceVariables = Collections.synchronizedMap(new HashMap<>());

    Map<String, DeviceModel> deviceModelMap = Collections.synchronizedMap(new HashMap<>());

    Map<String, String> sessionPowerStatusMap = Collections.synchronizedMap(new HashMap<>());

    @Value("${openpos.general.authToken:#{null}}")
    String serverAuthToken;

    @Value("${openpos.general.compatibility.version:#{null}}")
    String serverCompatibilityVersion;

    @Autowired(required = false)
    PersonalizationParameters personalizationParameters;

    @Autowired(required = false)
    ClientContextConfig clientContextConfig;

    @Autowired
    IDevicesService devicesService;

    @SneakyThrows
    public void onApplicationEvent(SessionConnectEvent event) {
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        String authToken = getHeader(event.getMessage(), "authToken");
        String deviceToken = getHeader(event.getMessage(), "deviceToken");
        String clientVersions = getHeader(event.getMessage(), "version");
        String boxLog = BoxLogging.box(String.format("Session Connected %s", sessionId));
        log.info("{}\n{}\n", boxLog, clientVersions);
        String compatibilityVersion = getHeader(event.getMessage(), COMPATIBILITY_VERSION_HEADER);
        String queryParams = getHeader(event.getMessage(), QUERY_PARAMS_HEADER);
        String powerStatus = getHeader(event.getMessage(), POWER_STATUS_HEADER);

        List<Version> deviceVersions = new ArrayList<>();
        if (clientVersions != null) {
            deviceVersions = DefaultObjectMapper.defaultObjectMapper().readValue(clientVersions, new TypeReference<List<Version>>() {
            });
        }

        sessionAppIdMap.put(sessionId, getHeader(event.getMessage(), APPID_HEADER));
        sessionQueryParamsMap.put(sessionId, toQueryParams(queryParams, deviceVersions));
        sessionPowerStatusMap.put(sessionId, powerStatus);

        DeviceModel deviceModel = devicesService.authenticateDevice(
                AuthenticateDeviceRequest.builder()
                        .authToken(deviceToken)
                        .build()).getDeviceModel();

        deviceModelMap.put(sessionId, deviceModel);

        if (deviceModel == null) {
            this.log.warn("Device is not personalized");
        }

        sessionAuthenticated.put(sessionId, (isBlank(serverAuthToken) || serverAuthToken.equals(authToken)) && deviceModel != null);
        if ((isNotBlank(serverAuthToken) && !serverAuthToken.equals(authToken)) || deviceModel == null) {
            String clientAuthTokenValueIfNull =
                    authToken == null || "".equals(authToken) || "undefined".equals(authToken)
                    ? String.format(" (value is: '%s')", authToken)
                    : "";
            this.log.warn("Client auth token{} does not match server auth token, client connection will be rejected.", clientAuthTokenValueIfNull);
        }
        sessionCompatible.put(sessionId, serverCompatibilityVersion == null || serverCompatibilityVersion.equals(compatibilityVersion));

        setPersonalizationResults(sessionId, deviceModel);
        setDeviceVariables(sessionId, event);
    }

    private void setDeviceVariables(String sessionId, SessionConnectEvent event) {
        if(clientContextConfig != null && clientContextConfig.getParameters() != null) {
            Map<String, String> context = new HashMap<>();
            for (String param : clientContextConfig.getParameters()) {
                String value = getHeader(event.getMessage(), param);
                if (value != null) {
                    context.put(param, value);
                } else {
                    context.put(param, "?");
                }
            }
            deviceVariables.put(sessionId, context);
        }
    }

    private void setPersonalizationResults(String sessionId, DeviceModel deviceModel) {
        if (personalizationParameters != null && personalizationParameters.getParameters() != null) {
            Map<String, String> personalizationResults = new HashMap<>();
            for (PersonalizationParameter param : personalizationParameters.getParameters()) {
                String prop = param.getProperty();
                deviceModel.getDeviceParamModels().stream()
                        .filter(deviceParamModel -> deviceParamModel.getParamName().equals(prop))
                        .findFirst()
                        .ifPresent(paramModel -> personalizationResults.put(prop, paramModel.getParamValue()));
            }
            if (! personalizationResults.containsKey(ClientContext.BUSINESS_UNIT_ID)) {
                personalizationResults.put(ClientContext.BUSINESS_UNIT_ID, deviceModel.getBusinessUnitId());
            }
            sessionPersonalizationResults.put(sessionId, personalizationResults);
        }
    }

    private Map<String, Object> toQueryParams(String json, List<Version> deviceVersions) {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        try {
            Map<String, Object> map = DefaultObjectMapper.build().readValue(json, typeRef);
            map.put("deviceVersions", deviceVersions);
            return map;
        } catch (Exception e) {
            log.error("Failed to parse query params", e);
            return Collections.emptyMap();
        }
    }

    public boolean isSessionAuthenticated(String sessionId) {
        return this.sessionAuthenticated.get(sessionId) != null && this.sessionAuthenticated.get(sessionId);
    }

    public boolean isSessionCompatible(String sessionId) {
        return this.sessionCompatible.get(sessionId) != null && this.sessionCompatible.get(sessionId);
    }

    public Map<String, String> getPersonalizationResults(String sessionId) {
        return sessionPersonalizationResults.get(sessionId);
    }

    public Map<String, Object> getQueryParams(String sessionId) {
        return sessionQueryParamsMap.get(sessionId);
    }

    public String getPowerStatus(String sessionId) {
        return sessionPowerStatusMap.get(sessionId);
    }

    public void removeSession(String sessionId) {
        this.sessionAuthenticated.remove(sessionId);
        this.sessionCompatible.remove(sessionId);
    }

    public Map<String, String> getDeviceVariables(String sessionId) { 
        return deviceVariables.get(sessionId); 
    }

    public DeviceModel getDeviceModel(String sessionId) {
        return deviceModelMap.get(sessionId);
    }

}