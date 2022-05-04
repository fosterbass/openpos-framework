package org.jumpmind.pos.devices.service;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.GetOrphanedDevicesRequest;
import org.jumpmind.pos.devices.service.model.GetOrphanedDevicesResponse;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/orphaned")
public class GetOrphanedDevicesEndpoint {
    @Autowired
    DevicesRepository devicesRepository;

    public GetOrphanedDevicesResponse getOrphanedDevices(GetOrphanedDevicesRequest request) {
        List<DeviceModel> unpairedDevices;

        if (StringUtils.isBlank(request.getAppId())) {
            unpairedDevices = devicesRepository.getOrphanedDevices(request.getBusinessUnitId());
        } else {
            unpairedDevices = devicesRepository.getOrphanedDevicesByAppId(request.getBusinessUnitId(), request.getAppId());
        }

        return new GetOrphanedDevicesResponse(unpairedDevices);
    }
}
