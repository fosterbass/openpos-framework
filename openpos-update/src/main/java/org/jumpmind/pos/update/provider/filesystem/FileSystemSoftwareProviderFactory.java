package org.jumpmind.pos.update.provider.filesystem;

import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.jumpmind.pos.update.provider.ISoftwareProviderFactory;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("filesystem.softwareprovider")
public class FileSystemSoftwareProviderFactory implements ISoftwareProviderFactory {
    @Autowired
    private Environment env;

    @Override
    public ISoftwareProvider createProvider(PackageVersioning versioning, String configKey) {
        final Binder binder = Binder.get(env);
        final BindResult<FileSystemSoftwareProviderConfiguration> config = binder.bind(configKey, FileSystemSoftwareProviderConfiguration.class);

        if (!config.isBound()) {
            throw new IllegalArgumentException("configuration key has no valid configuration");
        }

        return new FileSystemSoftwareProvider(versioning, config.get());
    }
}
