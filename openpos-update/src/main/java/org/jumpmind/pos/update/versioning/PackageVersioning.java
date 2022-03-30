package org.jumpmind.pos.update.versioning;

import lombok.extern.slf4j.Slf4j;

// just a simple wrapper around a version factory to mainly deal with generics
// without having to incur warnings or overly verbose code.
@Slf4j
public class PackageVersioning {
    IVersionFactory<? extends  Version> factory;

    public PackageVersioning(IVersionFactory<? extends  Version> factory) {
        this.factory = factory;
    }

    public Version fromString(String version) throws IllegalArgumentException {
        return factory.fromString(version);
    }
}
