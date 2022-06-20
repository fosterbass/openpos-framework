package org.jumpmind.pos.devices.service;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.DeviceNotFoundException;
import org.jumpmind.pos.devices.extensibility.BusinessUnitInfo;
import org.jumpmind.pos.devices.extensibility.IBusinessUnitProvider;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DeviceParamModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.PersonalizationConfigDevice;
import org.jumpmind.pos.devices.service.model.PersonalizationConfigResponse;
import org.jumpmind.pos.devices.service.model.PersonalizationParameters;
import org.jumpmind.pos.devices.service.strategy.AcceptedPersonalizationBusinessUnit;
import org.jumpmind.pos.service.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/personalizationConfig")
public class GetPersonalizationConfigEndpoint {

    @Autowired(required=false)
    PersonalizationParameters personalizationParameters;
    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${openpos.businessunitId:undefined}")
    String businessUnitId;

    @Value("${openpos.installationId:'not set'}")
    String installationId;

    @Autowired
    DevicesRepository repository;

    @Autowired(required = false)
    List<String> loadedAppIds;

    @Autowired(required = false)
    IBusinessUnitProvider businessUnitProvider;

    @Autowired
    AcceptedPersonalizationBusinessUnit configuredBusinessUnits;

    public PersonalizationConfigResponse getPersonalizationConfig() {
        logger.debug("personalization config requested");

        if (personalizationParameters == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No personalization configuration, use default", null);
        }

        final Set<String> allowedBusinessUnits = configuredBusinessUnits.getAllowedBusinessUnits();

        List<BusinessUnitInfo> availableBusinessUnits = businessUnitProvider != null
                ? businessUnitProvider.getBusinessUnits().stream().filter(bu -> allowedBusinessUnits.contains(bu.getId())).collect(Collectors.toList())
                : null;

        // coerce any null value into a empty list.
        if (availableBusinessUnits == null) {
            availableBusinessUnits = new ArrayList<>();
        }

        final List<DeviceModel> allDevices = repository.findDevices();
        final Set<String> connectedDevices = repository.getConnectedDeviceIds();
        final Map<String, List<PersonalizationConfigDevice>> storeDevices = allDevices
                .stream()
                .filter(ds -> StringUtils.isNotBlank(ds.getBusinessUnitId()))
                .filter(ds -> loadedAppIds.contains(ds.getAppId()))
                .filter(ds -> allowedBusinessUnits.contains(ds.getBusinessUnitId()))
                .map(ds -> {
                    String authToken = null;

                    try {
                        authToken = repository.getDeviceAuth(ds.getDeviceId());
                    } catch (DeviceNotFoundException ignored) {
                        // the device doesn't have an auth token registered yet.
                    }

                    final Map<String, String> deviceParams = ds.getDeviceParamModels()
                            .stream()
                            .collect(Collectors.toMap(
                                    DeviceParamModel::getParamName,
                                    DeviceParamModel::getParamValue
                            ));

                    return PersonalizationConfigDevice.builder()
                            .businessUnitId(ds.getBusinessUnitId())
                            .deviceId(ds.getDeviceId())
                            .appId(ds.getAppId())
                            .authToken(authToken)
                            .personalizationParamValues(deviceParams)
                            .connected(connectedDevices.contains(ds.getDeviceId()))
                            .build();
                })
                .collect(Collectors.toMap(
                        PersonalizationConfigDevice::getBusinessUnitId,
                        (v) -> Stream.of(v).collect(Collectors.toList()),
                        (l, r) -> {
                            l.addAll(r);
                            return l;
                        }
                ));

        storeDevices.forEach((key, value) -> {
            value.sort(Comparator.comparing(PersonalizationConfigDevice::getDeviceId));
        });

        return PersonalizationConfigResponse.builder()
                .availableBusinessUnits(availableBusinessUnits)
                .storeDevices(storeDevices)
                .parameters(personalizationParameters.getParameters())
                .loadedAppIds(loadedAppIds)
                .build();
    }
}
