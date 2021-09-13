package org.jumpmind.pos.update.provider;

import org.jumpmind.pos.update.versioning.Version;

import java.nio.file.Path;
import java.util.List;

public interface ISoftwareProvider {
    List<Version> getAvailableVersions();
    Version getLatestVersion();
    Path getSoftwareVersion(Version version);
}
