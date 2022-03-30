package org.jumpmind.pos.update.versioning.noop;


import org.jumpmind.pos.update.versioning.IVersionFactory;
import org.jumpmind.pos.update.versioning.Version;
import org.springframework.stereotype.Component;

@Component("noop.versionfactory")
public class NoopVersionFactory implements IVersionFactory<Version> {
    @Override
    public Version fromString(String version) {
        throw new IllegalArgumentException();
    }
}
