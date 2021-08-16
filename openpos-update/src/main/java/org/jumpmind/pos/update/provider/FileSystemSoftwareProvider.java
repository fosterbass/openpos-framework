package org.jumpmind.pos.update.provider;

import org.jumpmind.pos.update.UpdateModule;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Profile(UpdateModule.NAME)
@Component
public class FileSystemSoftwareProvider implements ISoftwareProvider {

    @Override
    public List<String> getAvailableVersions() {
        return null;
    }

    @Override
    public String getLatestVersion() {
        return null;
    }

    @Override
    public Path getSoftwareVersion(String version) {
        return null;
    }

}
