package org.jumpmind.pos.devices.service;

import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.model.SetBrandRequest;
import org.jumpmind.pos.devices.service.model.SetBrandResponse;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

@Endpoint(path = "/devices/setBrand")
public class SetBrandEndpoint {

    @Autowired
    DevicesRepository devicesRepository;

    public SetBrandResponse setBrand(@RequestBody SetBrandRequest request) {
        devicesRepository.setBrand(request.getDeviceId(), request.getNewBrand());
        return SetBrandResponse.builder().device(devicesRepository.getDevice(request.getDeviceId())).build();
    }
}
