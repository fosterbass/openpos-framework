package org.jumpmind.pos.service;

import org.jumpmind.pos.service.strategy.InvocationStrategy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.NONE;

@Component
@Getter
@Setter
public class ServiceSpecificConfig implements Cloneable {

    @Autowired(required = false)
    @Getter(NONE)
    @Setter(NONE)
    IConfigApplicator additionalConfigSource;

    private List<String> profileIds;
    private InvocationStrategy strategy = InvocationStrategy.LOCAL_ONLY;
    private List<EndpointSpecificConfig> endpoints = new ArrayList<>();
    private SamplingConfig samplingConfig;

    public ServiceSpecificConfig copy() {
        ServiceSpecificConfig copy;
        try {
            copy = (ServiceSpecificConfig)this.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void findAdditionalConfigs(String serviceId) {
        if (samplingConfig == null) {
            samplingConfig = new SamplingConfig();
        }
        if(additionalConfigSource != null) {
            String startsWith = String.format("openpos.services.specificConfig.%s.samplingConfig", serviceId);
            additionalConfigSource.applyAdditionalConfiguration(startsWith, samplingConfig);

            for (int index = 0; index < endpoints.size(); index++) {
                endpoints.get(index).findAdditionalConfigs(serviceId, index);
                startsWith = String.format("openpos.services.specificConfig.%s.endpoints[%d]", serviceId, index);
                additionalConfigSource.applyAdditionalConfiguration(startsWith, endpoints.get(index));
            }
        }
    }

    public boolean isSamplingEnabled() {
        return (samplingConfig != null) && samplingConfig.isEnabled();
    }
}
