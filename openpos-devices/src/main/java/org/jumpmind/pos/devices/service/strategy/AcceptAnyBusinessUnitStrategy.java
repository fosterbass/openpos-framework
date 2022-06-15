package org.jumpmind.pos.devices.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.devices.extensibility.BusinessUnitInfo;
import org.jumpmind.pos.devices.extensibility.IBusinessUnitProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component("AcceptAnyBusinessUnitStrategy")
public class AcceptAnyBusinessUnitStrategy implements IAcceptedPersonalizationBusinessUnitStrategy {
    @Autowired
    IBusinessUnitProvider provider;

    @Override
    public Set<String> getAllowedBusinessUnits() {
        return provider.getBusinessUnits().stream()
                .map(BusinessUnitInfo::getId)
                .collect(Collectors.toSet());
    }
}
