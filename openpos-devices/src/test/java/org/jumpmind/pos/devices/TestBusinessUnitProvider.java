package org.jumpmind.pos.devices;

import lombok.Getter;
import lombok.Setter;
import org.jumpmind.pos.devices.extensibility.BusinessUnitInfo;
import org.jumpmind.pos.devices.extensibility.IBusinessUnitProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Component
@Profile("test-business-unit-provider")
public class TestBusinessUnitProvider implements IBusinessUnitProvider {
    List<BusinessUnitInfo> knownBusinessUnits = new ArrayList<>(Collections.singletonList(
            BusinessUnitInfo.builder()
                    .id("00110")
                    .name("Test")
                    .locationHint("Columbus, OH, USA")
                    .build()
    ));

    @Override
    public List<BusinessUnitInfo> getBusinessUnits() {
        return knownBusinessUnits;
    }
}
