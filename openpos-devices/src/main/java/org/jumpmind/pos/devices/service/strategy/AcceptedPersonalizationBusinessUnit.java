package org.jumpmind.pos.devices.service.strategy;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class AcceptedPersonalizationBusinessUnit {
    @Resource(name = "${openpos.personalization.deviceBusinessUnitIdStrategy:AcceptConfiguredBusinessUnitStrategy}")
    protected IAcceptedPersonalizationBusinessUnitStrategy deviceBusinessUnitIdStrategy;

    public Set<String> getAllowedBusinessUnits() {
        return deviceBusinessUnitIdStrategy.getAllowedBusinessUnits();
    }
}
