package org.jumpmind.pos.update.provider;

import bsh.commands.dir;
import org.jumpmind.pos.update.UpdateModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.*;

@Profile(UpdateModule.NAME)
@Component
public class FileSystemSoftwareProvider implements ISoftwareProvider {

    @Value("${openpos.update.fileSystemSoftwareProvider.artifactExtension}")
    String artifactExtension;

    @Value("${openpos.update.fileSystemSoftwareProvider.baseDir}")
    String baseDir;

    @Override
    public List<String> getAvailableVersions() {
        List<String> versions = new ArrayList<>();
        File[] files = getFiles();
        for (File file : files) {
            String version = parseVersion(file.getName());
            if (isNotBlank(version)) {
                versions.add(version);
            }
        }
        Collections.sort(versions);
        return versions;
    }

    @Override
    public String getLatestVersion() {
        List<String> versions = getAvailableVersions();
        if (versions.size() > 0) {
            return versions.get(versions.size()-1);
        } else {
            return null;
        }
    }

    @Override
    public Path getSoftwareVersion(String version) {
        Path path = null;
        File[] files = getFiles();
        for (File file : files) {
            String currentVersion = parseVersion(file.getName());
            if (isNotBlank(currentVersion) && currentVersion.equals(version)) {
                path = Paths.get(file.toURI());
                break;
            }
        }
        return path;
    }

    String parseVersion(String fileName) {
        return fileName.replaceAll(".*?((?<!\\w)\\d+([.-]\\d+)*).*", "$1");
    }

    File[] getFiles() {
        return new File(baseDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("." + artifactExtension);
            }
        });
    }

}
