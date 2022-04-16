package org.jumpmind.pos.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

public class ArtifactVersion {
    public static final int MAJOR_INDEX = 0;
    public static final int MINOR_INDEX = 1;
    public static final int PATCH_INDEX = 2;
    private String version = null;
    private long buildTime = -1;
    private String buildYear;
    private String artifactName;

    @Builder
    public ArtifactVersion(String artifactName) {
        this.artifactName = artifactName;
    }

    protected Attributes findManifestAttributes() {
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(
                    "META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try (InputStream is = resources.nextElement().openStream()) {
                    Manifest manifest = new Manifest(is);
                    Attributes attributes = manifest.getMainAttributes();
                    if (artifactName.equals(attributes.getValue("Project-Artifact"))) {
                        return attributes;
                    }
                }
            }
        } catch (IOException e) {
            // nothing to do, really
        }
        return null;
    }

    public String version() {
        if (version == null) {
            Attributes attributes = findManifestAttributes();
            if (attributes != null) {
                version = attributes.getValue("Build-Version");
            } else {
                File dir = new File("../");
                File[] files = dir.listFiles();
                for (File file: files) {
                    if (file.isDirectory() && file.getName().endsWith("-assemble")) {
                        File gradleProperties = new File(file, "gradle.properties");
                        if (gradleProperties.exists()) {
                            Properties props = new Properties();
                            try (FileReader fileReader = new FileReader(gradleProperties)) {
                                props.load(fileReader);
                                version = props.getProperty("version");
                            } catch (IOException ex) {
                            }
                        }
                    }
                }

                if (version == null) {
                    version = "1000.0.0";
                }
            }

            if (version.endsWith("-SNAPSHOT")) {
                return version.substring(0, version.length()-"-SNAPSHOT".length());
            }
        }
        return version;
    }

    public long getBuildTime() {
        if (buildTime == -1) {
            Attributes attributes = findManifestAttributes();
            try {
                buildTime = Long.parseLong(attributes.getValue("Build-Time").split("-")[0]);
            } catch (Exception e) {
                buildTime = 0;
            }
        }
        return buildTime;
    }

    public String getBuildYear() {
        if (buildYear == null) {
            Attributes attributes = findManifestAttributes();
            try {
                buildYear = attributes.getValue("Build-Time").substring(0, 4);
            } catch (Exception e) {
                buildYear = "";
            }
        }
        return buildYear;
    }

    public String versionWithUnderscores() {
        return version().replace("[\\.\\-]", "_");
    }

    public static int[] parseVersion(String version) {
        version = version.replaceAll("[^0-9\\.]", "");
        int[] versions = new int[3];
        if (!StringUtils.isEmpty(version)) {
            String[] splitVersion = version.split("\\.");
            if (splitVersion.length >= 3) {
                versions[PATCH_INDEX] = parseVersionComponent(splitVersion[2]);
            }
            if (splitVersion.length >= 2) {
                versions[MINOR_INDEX] = parseVersionComponent(splitVersion[1]);
            }
            if (splitVersion.length >= 1) {
                versions[MAJOR_INDEX] = parseVersionComponent(splitVersion[0]);
            }
        }
        return versions;
    }

    private static int parseVersionComponent(String versionComponent) {
        int version = 0;
        try {
            version = Integer.parseInt(versionComponent);
        } catch (NumberFormatException e) {
        }
        return version;
    }

    public boolean isOlderMajorVersion(String version) {
        return isOlderMajorVersion(parseVersion(version));
    }

    private boolean isOlderMajorVersion(int[] versions) {
        int[] softwareVersion = parseVersion(version());
        if (versions[MAJOR_INDEX] < softwareVersion[MAJOR_INDEX]) {
            return true;
        }
        return false;
    }

    public boolean isOlderVersion(String version) {
        return isOlderThanVersion(version, version());
    }

    public static boolean isOlderThanVersion(String checkVersion, String targetVersion) {
        if (noVersion(targetVersion) || noVersion(checkVersion)) {
            return false;
        }
        int[] checkVersions = parseVersion(checkVersion);
        int[] targetVersions = parseVersion(targetVersion);
        return isOlderThanVersion(checkVersions, targetVersions);
    }

    public static boolean isOlderThanVersion(int[] checkVersion, int[] targetVersion) {
        if (checkVersion == null || targetVersion == null) {
            return false;
        }
        if (checkVersion[MAJOR_INDEX] < targetVersion[MAJOR_INDEX]) {
            return true;
        } else if (checkVersion[MAJOR_INDEX] == targetVersion[MAJOR_INDEX]
                && checkVersion[MINOR_INDEX] < targetVersion[MINOR_INDEX]) {
            return true;
        } else if (checkVersion[MAJOR_INDEX] == targetVersion[MAJOR_INDEX]
                && checkVersion[MINOR_INDEX] == targetVersion[MINOR_INDEX]
                && checkVersion[PATCH_INDEX] < targetVersion[PATCH_INDEX]) {
            return true;
        }
        return false;
    }

    protected static boolean noVersion(String targetVersion) {
        return StringUtils.isBlank(targetVersion) || "development".equals(targetVersion);
    }

    public boolean isOlderMinorVersion(String oldVersion) {
        return isOlderMinorVersion(oldVersion, version());
    }

    public boolean isOlderMinorVersion(String checkVersion, String targetVersion) {
        if (noVersion(targetVersion) || noVersion(checkVersion)) {
            return false;
        }
        int[] checkVersions = parseVersion(checkVersion);
        int[] targetVersions = parseVersion(targetVersion);
        return isOlderMinorVersion(checkVersions, targetVersions);
    }

    public boolean isOlderMinorVersion(int[] checkVersion, int[] targetVersion) {
        if (checkVersion[MAJOR_INDEX] < targetVersion[MAJOR_INDEX]) {
            return true;
        } else if (checkVersion[MAJOR_INDEX] == targetVersion[MAJOR_INDEX]
                && checkVersion[MINOR_INDEX] < targetVersion[MINOR_INDEX]) {
            return true;
        }
        return false;
    }
}