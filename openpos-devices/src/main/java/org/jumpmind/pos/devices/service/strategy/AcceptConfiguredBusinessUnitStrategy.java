package org.jumpmind.pos.devices.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component("AcceptConfiguredBusinessUnitStrategy")
public class AcceptConfiguredBusinessUnitStrategy implements IAcceptedPersonalizationBusinessUnitStrategy {
    @Value("${openpos.businessunitId:undefined}")
    private String businessUnitId;

    @Override
    public Set<String> getAllowedBusinessUnits() {
        if (StringUtils.equals("undefined", businessUnitId)) {
            log.error("`openpos.businessunitId` not specified; required for strategy to function");
            return Collections.emptySet();
        }

        return Stream.of(businessUnitId).collect(Collectors.toSet());
    }
}
