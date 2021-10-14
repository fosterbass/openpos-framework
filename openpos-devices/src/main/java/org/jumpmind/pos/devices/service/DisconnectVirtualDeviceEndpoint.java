package org.jumpmind.pos.devices.service;

import org.jumpmind.pos.devices.model.VirtualDeviceRepository;
import org.jumpmind.pos.devices.service.model.DisconnectDeviceRequest;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/disconnectDevice", implementation = "virtual")
public class DisconnectVirtualDeviceEndpoint {

    @Autowired
    VirtualDeviceRepository devicesRepository;

    public void disconnectDevice(@RequestBody DisconnectDeviceRequest request) {
        devicesRepository.removeByDeviceId(request.getDeviceId());
    }
}
