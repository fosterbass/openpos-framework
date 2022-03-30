package org.jumpmind.pos.update.versioning;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.update.provider.SoftwareProvider;
import org.jumpmind.pos.update.versioning.noop.NoopVersionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class Versioning {
    @Autowired
    Environment env;

    @Autowired
    Map<String, IVersionFactory<? extends Version>> versionFactories;

    @Autowired
    SoftwareProvider provider;

    public PackageVersioning forPackage(String packageName) {
        final PackageVersioning versioning = provider.getVersioningFor(packageName);

        if (versioning == null) {
            return new PackageVersioning(new NoopVersionFactory());
        }

        return versioning;
    }
}
