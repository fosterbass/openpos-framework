package org.jumpmind.pos.update.provider.google_cloud_storage;

import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.jumpmind.pos.update.provider.ISoftwareProviderFactory;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("google_cloud_storage.softwareprovider")
public class GoogleCloudStorageSoftwareProviderFactory implements ISoftwareProviderFactory {
    @Autowired
    private Environment env;

    @Override
    public ISoftwareProvider createProvider(PackageVersioning versioning, String configKey) {
        final Binder binder = Binder.get(env);
        final BindResult<GoogleCloudStorageSoftwareProviderConfiguration> config = binder.bind(configKey, GoogleCloudStorageSoftwareProviderConfiguration.class);

        if (!config.isBound()) {
            throw new IllegalArgumentException("configuration key has no valid configuration");
        }

        return new GoogleCloudStorageSoftwareProvider(versioning, config.get());
    }
}
