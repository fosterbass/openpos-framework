package org.jumpmind.pos.update.provider;

import org.jumpmind.pos.update.versioning.PackageVersioning;

public interface ISoftwareProviderFactory {
    ISoftwareProvider createProvider(PackageVersioning versioning, String configKey);
}
