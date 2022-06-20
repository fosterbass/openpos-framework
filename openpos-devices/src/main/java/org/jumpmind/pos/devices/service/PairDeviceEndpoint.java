package org.jumpmind.pos.devices.service;

import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.PairDeviceRequest;
import org.jumpmind.pos.devices.service.model.PairDeviceResponse;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/pair")
public class PairDeviceEndpoint {
    @Autowired
    DevicesRepository devicesRepository;

    public PairDeviceResponse pairDevice(PairDeviceRequest request) {
        devicesRepository.pairDevice(
                request.getParent(),
                request.getChild()
        );

        return PairDeviceResponse.builder()
                .parent(devicesRepository.getDevice(request.getParent()))
                .child(devicesRepository.getDevice(request.getChild()))
                .build();
    }
}
