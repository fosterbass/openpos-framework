package org.jumpmind.pos.update.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;

@Component
@Lazy
public class FileSystemSoftwareProvider implements ISoftwareProvider {

    @Value("${openpos.update.fileSystemSoftwareProvider.artifactExtension:zip}")
    String artifactExtension;

    @Value("${openpos.update.fileSystemSoftwareProvider.baseDir:/}")
    String baseDir;

    @Value("${openpos.update.fileSystemSoftwareProvider.namePattern:^(\\w+-)+(?<version>(\\d+)(\\.(\\d+)(\\.(\\d+))?)?(-+([\\w-]+[\\w\\-.]+))?(\\+([\\w-]+[\\w\\-.]+))?)\\.zip$}")
    String fileNamePattern;

    @Value("${openpos.update.fileSystemSoftwareProvider.namePatternIgnoreCase:false}")
    boolean fileNamePatternIgnoresCase;

    @Autowired
    Versioning versionFactory;

    private Pattern compiledFileNamePattern;

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

        Stream<FileVersionPair> result;

        if (StringUtils.isNotEmpty(fileNamePattern)) {
            if (compiledFileNamePattern == null) {
                int flags = 0;

                if (fileNamePatternIgnoresCase) {
                    flags |= Pattern.CASE_INSENSITIVE;
                }

                compiledFileNamePattern = Pattern.compile(fileNamePattern, flags);
            }

            result = files.map(f -> {
                Version resultingVersion = null;

                final Matcher matcher = compiledFileNamePattern.matcher(f.getName());
                if (matcher.find()) {
                    final String versionString = matcher.group("version");

                    if (versionString != null) {
                        try {
                            resultingVersion = versionFactory.fromString(versionString);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }

                return new FileVersionPair(f, resultingVersion);
            });
        } else {
            // assume the file name is named after the version number.
            result = files.map(f -> {
               Version resultingVersion = null;

               try {
                   resultingVersion = versionFactory.fromString(f.getName());
               } catch (IllegalArgumentException ignored) {
               }

               return new FileVersionPair(f, resultingVersion);
            });
        }

        return result.filter(f -> f.getVersion() != null);
    }
}
