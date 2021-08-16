package org.jumpmind.pos.update.provider;

import java.nio.file.Path;
import java.util.List;

public interface ISoftwareProvider {

    List<String> getAvailableVersions();
    String getLatestVersion();
    Path getSoftwareVersion(String version);

}
