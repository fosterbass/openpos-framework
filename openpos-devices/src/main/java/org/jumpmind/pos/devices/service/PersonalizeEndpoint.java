package org.jumpmind.pos.devices.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.DeviceNotAuthorizedException;
import org.jumpmind.pos.devices.DeviceNotFoundException;
import org.jumpmind.pos.devices.DeviceUpdater;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DeviceParamModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.PersonalizationRequest;
import org.jumpmind.pos.devices.service.model.PersonalizationResponse;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Slf4j
@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/personalize")
public class PersonalizeEndpoint {

    @Autowired
    DevicesRepository devicesRepository;

    @Autowired
    DeviceUpdater deviceUpdater;

    @Autowired
    IDevicesService devicesService;

    public PersonalizationResponse personalize(@RequestBody PersonalizationRequest request) {
        String authToken = request.getDeviceToken();
        final String deviceId = request.getDeviceId();
        final String appId = request.getAppId();
        final String parentAppId = request.getParentAppId();
        final String parentDeviceId = request.getParentDeviceId();

        DeviceModel deviceModel;

        if (isNotBlank(deviceId) && isNotBlank(appId)) {
            try {
                log.info("Validating auth request of {} as {}", deviceId, appId);
                String auth = devicesRepository.getDeviceAuth(request.getDeviceId());

                if (authToken != null) {
                    if(!auth.equals(authToken)) {
                        throw new DeviceNotAuthorizedException();
                    }
                } else {
                    deviceModel = devicesRepository.getDevice(deviceId);
                    if (appId.equals(deviceModel.getAppId())) {
                        // Allow re-personalization for same device, same app id when token is omitted.
                        // Spares us from needing to manually delete personalization rows from DB when same device is being re-personalized.
                        throw new DeviceNotFoundException("token is null, re-personalizing existing device");
                    } else {
                        throw new DeviceNotAuthorizedException(String.format("Device '%s' is currently personalized for appId '%s'", deviceId, deviceModel.getAppId()));
                    }
                }

            } catch (DeviceNotFoundException ex) {
                log.info("Registering {} as {}", deviceId, appId);
                // if device doesn't exist create a new unique code
                authToken = UUID.randomUUID().toString();
                devicesRepository.saveDeviceAuth(deviceId, authToken);
            }

            deviceModel = new DeviceModel();
            deviceModel.setAppId(appId);
            deviceModel.setDeviceId(deviceId);

            if (request.getPersonalizationParameters() != null) {
                deviceModel.setDeviceParamModels(
                        request.getPersonalizationParameters().keySet().stream().map(key -> new DeviceParamModel(key, request.getPersonalizationParameters().get(key))).collect(Collectors.toList())
                );
            }
        } else if (isNotBlank(authToken)) {
            deviceModel = devicesRepository.getDeviceByAuth(authToken);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DeviceId and AppId or AuthToken are required for personalization");
        }

        // ensure the paired device actually exists before trying to pair with it
        DeviceModel parentDeviceModel = null;
        if (StringUtils.isNotBlank(parentAppId) && StringUtils.isNotBlank(parentDeviceId)) {
            parentDeviceModel = devicesRepository.getDevice(parentDeviceId);

            if (parentDeviceModel == null) {
                parentDeviceModel = DeviceModel.builder()
                        .deviceId(parentDeviceId)
                        .appId(parentAppId)
                        .deviceParamModels(
                                request.getPersonalizationParameters() != null
                                        ? request.getPersonalizationParameters().keySet().stream().map(key -> new DeviceParamModel(key, request.getPersonalizationParameters().get(key))).collect(Collectors.toList())
                                        : null
                        )
                        .build();
            }

            deviceUpdater.updateDevice(parentDeviceModel);
        }

        deviceUpdater.updateDevice(deviceModel);

        if (parentDeviceModel != null) {
            devicesRepository.pairDevice(parentDeviceModel.getDeviceId(), deviceModel.getDeviceId());
            deviceModel = devicesRepository.getDevice(deviceModel.getDeviceId());
        }

        return PersonalizationResponse.builder()
                .authToken(authToken)
                .deviceModel(deviceModel)
                .build();
    }
}
