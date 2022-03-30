package org.jumpmind.pos.update.provider.artifactory;

import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.jumpmind.pos.update.provider.ISoftwareProviderFactory;
import org.jumpmind.pos.update.provider.filesystem.FileSystemSoftwareProvider;
import org.jumpmind.pos.update.provider.filesystem.FileSystemSoftwareProviderConfiguration;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("artifactory.softwareprovider")
public class ArtifactorySoftwareProviderFactory implements ISoftwareProviderFactory {
    @Autowired
    private Environment env;

    @Override
    public ISoftwareProvider createProvider(PackageVersioning versioning, String configKey) {
        final Binder binder = Binder.get(env);
        final BindResult<ArtifactorySoftwareProviderConfiguration> config = binder.bind(configKey, ArtifactorySoftwareProviderConfiguration.class);

        if (!config.isBound()) {
            throw new IllegalArgumentException("configuration key has no valid configuration");
        }

        return new ArtifactorySoftwareProvider(versioning, config.get());
    }
}
