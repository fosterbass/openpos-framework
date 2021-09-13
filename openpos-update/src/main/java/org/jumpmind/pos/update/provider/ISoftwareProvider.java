package org.jumpmind.pos.update.provider;

import java.nio.file.Path;
import java.util.List;

public interface ISoftwareProvider {
    List<Version> getAvailableVersions();
    Version getLatestVersion();
    Path getSoftwareVersion(Version version);
}
