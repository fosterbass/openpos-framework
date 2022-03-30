package org.jumpmind.pos.update.versioning.semver;

import org.jumpmind.pos.update.versioning.IVersionFactory;
import org.springframework.stereotype.Component;

@Component("semver.versionfactory")
public class SemanticVersionFactory implements IVersionFactory<SemanticVersion> {
    @Override
    public SemanticVersion fromString(String version) {
        return SemanticVersion
                .tryParse(version)
                .orElseThrow(() -> new IllegalArgumentException("unable to parse specified version"));
    }
}
