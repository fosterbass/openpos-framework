package org.jumpmind.pos.update.versioning;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class Versioning {
    @Value("${openpos.update.versioning}")
    String versioningProvider;

    @Autowired
    Map<String, IVersionFactory<? extends Version>> versionFactories;

    private static final NoopVersionFactory NOOP_VERSION_FACTORY = new NoopVersionFactory();

    public Version fromString(String version) {
        return getConfiguredFactory().fromString(version);
    }

    private IVersionFactory<? extends Version> getConfiguredFactory() {
        IVersionFactory<? extends Version> factory = NOOP_VERSION_FACTORY;

        if (versionFactories != null) {
            if (StringUtils.isNotEmpty(versioningProvider)) {
                IVersionFactory<? extends Version> configuredFactory = versionFactories.get(versioningProvider);

                if (configuredFactory != null) {
                    factory = configuredFactory;
                } else {
                    log.warn("versioning provider was configured to `{}`, however no provider with that name was found; won't know how to parse versions", versioningProvider);
                }
            } else {
                log.warn("no versioning provider specified, won't know how to parse versions");
            }
        }

        return factory;
    }

    private static class NoopVersionFactory implements IVersionFactory<Version> {
        @Override
        public Version fromString(String version) {
            throw new IllegalArgumentException();
        }
    }
}
