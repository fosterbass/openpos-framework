package org.jumpmind.pos.devices.service;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.GetUnpairedDevicesRequest;
import org.jumpmind.pos.devices.service.model.GetUnpairedDevicesResponse;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/unpaired")
public class GetUnpairedDevicesEndpoint {
    @Autowired
    DevicesRepository devicesRepository;

    public GetUnpairedDevicesResponse getUnpairedDevices(GetUnpairedDevicesRequest request) {
        List<DeviceModel> unpairedDevices;

        if (StringUtils.isBlank(request.getAppId())) {
            unpairedDevices = devicesRepository.getUnpairedDevices(request.getBusinessUnitId());
        } else {
            unpairedDevices = devicesRepository.getUnpairedDevicesByAppId(request.getBusinessUnitId(), request.getAppId());
        }

        return new GetUnpairedDevicesResponse(unpairedDevices);
    }
}
