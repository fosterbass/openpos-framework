package org.jumpmind.pos.devices.service;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.GetChildDevicesRequest;
import org.jumpmind.pos.devices.service.model.GetChildDevicesResponse;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/children")
public class GetChildDevicesEndpoint {
    @Autowired
    DevicesRepository devicesRepository;

    public GetChildDevicesResponse getChildDevices(@RequestBody GetChildDevicesRequest request) {
        List<DeviceModel> childDevices;

        if (StringUtils.isBlank(request.getAppId())) {
            childDevices = devicesRepository.getChildrenOf(request.getParentDeviceId());
        } else {
            childDevices = devicesRepository.getChildrenOfByAppId(request.getParentDeviceId(), request.getAppId());
        }

        return new GetChildDevicesResponse(childDevices);
    }
}
