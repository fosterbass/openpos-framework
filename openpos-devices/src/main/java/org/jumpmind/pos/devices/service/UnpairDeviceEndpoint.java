package org.jumpmind.pos.devices.service;

import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.UnpairDeviceRequest;
import org.jumpmind.pos.devices.service.model.UnpairDeviceResponse;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/unpair")
public class UnpairDeviceEndpoint {
    @Autowired
    DevicesRepository devicesRepository;

    public UnpairDeviceResponse unpairDevice(UnpairDeviceRequest request) {
        devicesRepository.unpairDevice(request.getChild());

        return UnpairDeviceResponse.builder()
                .child(devicesRepository.getDevice(request.getChild()))
                .build();
    }
}
