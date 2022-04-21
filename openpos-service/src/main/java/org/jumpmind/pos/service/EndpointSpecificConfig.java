package org.jumpmind.pos.service;

import org.jumpmind.pos.service.strategy.InvocationStrategy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static lombok.AccessLevel.NONE;

@Component
@Getter
@Setter
public class EndpointSpecificConfig implements Cloneable {

    @Getter(NONE)
    @Setter(NONE)
    @Autowired(required = false)
    IConfigApplicator additionalConfigSource;

    private String profile;
    private InvocationStrategy strategy;
    private String path;
    private SamplingConfig samplingConfig;

    public EndpointSpecificConfig copy() {
        try {
            return (EndpointSpecificConfig) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void findAdditionalConfigs(String serviceId, int index) {
        if (samplingConfig == null){
            samplingConfig = new SamplingConfig();
        }

        if (additionalConfigSource != null){
            String startsWith = String.format("openpos.services.specificConfig.%s.endpoints[%d].samplingConfig", serviceId, index);
            additionalConfigSource.applyAdditionalConfiguration(startsWith, samplingConfig);
        }
    }

    public boolean isSamplingEnabled() {
        return (samplingConfig != null) && samplingConfig.isEnabled();
    }
}
