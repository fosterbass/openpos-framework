package org.jumpmind.pos.update.versioning.basic;


import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.versioning.IVersionFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(UpdateModule.NAME)
@Component("basic")
public class BasicFactory implements IVersionFactory<Basic> {
    @Override
    public Basic fromString(String version) {
        return new Basic(version);
    }
}
