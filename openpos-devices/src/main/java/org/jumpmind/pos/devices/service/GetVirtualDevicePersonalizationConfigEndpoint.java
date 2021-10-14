package org.jumpmind.pos.devices.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.devices.service.model.PersonalizationConfigResponse;
import org.jumpmind.pos.service.Endpoint;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;


@Slf4j
@Endpoint(path = REST_API_CONTEXT_PATH + "/devices/personalizationConfig", implementation = "virtual")
public class GetVirtualDevicePersonalizationConfigEndpoint {

    public PersonalizationConfigResponse getPersonalizationConfig() {
        // Not Used
        return PersonalizationConfigResponse.builder().build();
    }
}
