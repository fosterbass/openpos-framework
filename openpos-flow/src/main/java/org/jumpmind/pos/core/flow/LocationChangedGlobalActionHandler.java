package org.jumpmind.pos.core.flow;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.core.service.ClientLocationService;
import org.jumpmind.pos.util.model.LocationData;
import org.jumpmind.pos.server.model.Action;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Slf4j
public class LocationChangedGlobalActionHandler {

    @In(scope = ScopeType.Device)
    IStateManager stateManager;

    @Autowired
    ClientLocationService clientLocationService;

    @OnGlobalAction
    public void onLocationChanged(Action action) {
        LocationData locationData = Action.convertActionData(action.getData(), LocationData.class);
        log.info("LocationData received: {}", Objects.toString(locationData, "null"));
        clientLocationService.setLocationData(locationData);

        stateManager.getDeviceVariables().put("locationData", locationData != null ? locationData.toString() : null);
    }

}
