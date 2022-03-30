package org.jumpmind.pos.update.versioning.basic;


import org.jumpmind.pos.update.versioning.IVersionFactory;
import org.springframework.stereotype.Component;

@Component("basic.versionfactory")
public class BasicFactory implements IVersionFactory<Basic> {
    @Override
    public Basic fromString(String version) {
        return new Basic(version);
    }
}
