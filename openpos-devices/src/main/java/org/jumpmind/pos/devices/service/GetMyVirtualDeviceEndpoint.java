package org.jumpmind.pos.devices.service;

import org.jumpmind.pos.devices.DeviceNotFoundException;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.VirtualDeviceRepository;
import org.jumpmind.pos.devices.service.model.GetDeviceResponse;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.springframework.beans.factory.annotation.Autowired;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/myDevice", implementation = "virtual")
public class GetMyVirtualDeviceEndpoint {

    @Autowired
    ClientContext clientContext;

    @Autowired
    VirtualDeviceRepository devicesRepository;

    public GetDeviceResponse getMyDevice() {
        DeviceModel deviceModel = devicesRepository.getByDeviceId(clientContext.get("deviceId"));
        if (deviceModel != null) {
            return GetDeviceResponse.builder()
                    .deviceModel(deviceModel)
                    .build();
        } else {
            throw new DeviceNotFoundException();
        }
    }
}
