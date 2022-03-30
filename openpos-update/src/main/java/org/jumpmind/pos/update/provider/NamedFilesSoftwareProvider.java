package org.jumpmind.pos.update.provider;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.jumpmind.pos.update.versioning.Version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NamedFilesSoftwareProvider implements ISoftwareProvider {
    protected final PackageVersioning versionFactory;

    private Pattern compiledFileNamePattern;
    private final String fileNamePattern;
    private final boolean fileNamePatternIgnoresCase;

    protected NamedFilesSoftwareProvider(
            String fileNamePattern,
            boolean fileNamePatternIgnoresCase,
            PackageVersioning versionFactory
    ) {
        this.versionFactory = versionFactory;

        this.fileNamePattern = fileNamePattern;
        this.fileNamePatternIgnoresCase = fileNamePatternIgnoresCase;
    }

    protected Version getVersionOfFile(String name) {
        Version resultingVersion = null;

        final Matcher matcher = getVersionPatternMatcher(name);

        if (matcher != null) {
            if (matcher.find()) {
                final String versionString = matcher.group("version");
                resultingVersion = parseVersionString(versionString);
            }
        } else {
            // assume the file name is named after the version number.
            resultingVersion = versionFactory.fromString(name);
        }

        return resultingVersion;
    }

    private Version parseVersionString(String version) {
        if (StringUtils.isNotBlank(version)) {
            try {
                return versionFactory.fromString(version);
            } catch (IllegalArgumentException ignored) {
                // ignored
            }
        }

        return null;
    }

    private Matcher getVersionPatternMatcher(String fileName) {
        final Pattern pattern = getCompiledFileNamePattern();
        if (pattern == null) {
            return null;
        }

        return pattern.matcher(fileName);
    }

    private Pattern getCompiledFileNamePattern() {
        if (StringUtils.isBlank(fileNamePattern)) {
            return null;
        }

        if (compiledFileNamePattern == null) {
            int flags = 0;

            if (fileNamePatternIgnoresCase) {
                flags |= Pattern.CASE_INSENSITIVE;
            }

            compiledFileNamePattern = Pattern.compile(fileNamePattern, flags);
        }

        return compiledFileNamePattern;
    }
}
