package org.jumpmind.pos.update.provider.filesystem;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jumpmind.pos.update.provider.NamedFilesSoftwareProvider;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.jumpmind.pos.update.versioning.Version;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;

public class FileSystemSoftwareProvider extends NamedFilesSoftwareProvider {
    String artifactExtension;
    String baseDir;

    public FileSystemSoftwareProvider(PackageVersioning versioning, FileSystemSoftwareProviderConfiguration config) {
        super(config.getFileNamePattern(), config.isFileNamePatternIgnoreCase(), versioning);

        artifactExtension = config.getArtifactType();
        baseDir = config.getBaseDirectory();
    }

    @Override
    public List<Version> getAvailableVersions() {
        return getFiles()
                .map(FileVersionPair::getVersion)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Version getLatestVersion() {
        return getFiles()
                .map(FileVersionPair::getVersion)
                .max(naturalOrder())
                .orElse(null);
    }

    @Override
    public Path getSoftwareVersion(Version version) {
        return getFiles()
                .filter(f -> f.getVersion().versionEquals(version))
                .findFirst()
                .map(f -> Paths.get(f.getFile().toURI()))
                .orElse(null);
    }

    @AllArgsConstructor
    @Data
    private static class FileVersionPair {
        private final File file;
        private final Version version;
    }

    Stream<FileVersionPair> getFiles() {
        final File[] filesList = new File(baseDir).listFiles((dir, name) -> name.endsWith("." + artifactExtension));
        final Stream<File> files = filesList != null ? Arrays.stream(filesList) : Stream.empty();

        return files
                .map(f -> new FileVersionPair(f, getVersionOfFile(f.getName())))
                .filter(f -> f.getVersion() != null);
    }
}
