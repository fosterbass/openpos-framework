package org.jumpmind.pos.update.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.model.Folder;
import org.jfrog.artifactory.client.model.Item;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Lazy
@Slf4j
public class ArtifactorySoftwareProvider implements ISoftwareProvider{

    @Value("${openpos.update.artifactorySoftwareProvider.url}")
    String artifactoryUrl;
    
    @Value("${openpos.update.artifactorySoftwareProvider.repositoryName}")
    String repositoryName;
    
    @Value("${openpos.update.artifactorySoftwareProvider.pathToFolder}")
    String pathToFolder;

    @Value("${openpos.update.artifactorySoftwareProvider.namePattern}")
    String fileNamePattern;

    @Value("${openpos.update.artifactorySoftwareProvider.namePatternIgnoreCase:false}")
    boolean fileNamePatternIgnoresCase;

    Artifactory artifactory;

    private Pattern compiledFileNamePattern;

    @Autowired
    Versioning versionFactory;

    private String tempDirectory;

    @PostConstruct
    public void init(){
        
        try {
            tempDirectory = Files.createTempDirectory("jmcUpdates").toFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        artifactory = ArtifactoryClientBuilder.create()
                .setUrl(artifactoryUrl)
                .build();
    }
    
    @Override
    public List<Version> getAvailableVersions() {
        Folder folder = artifactory.repository(repositoryName).folder(pathToFolder).info();
        return folder.getChildren().stream().map(item -> getVersionFromName(item.getName())).collect(Collectors.toList());
    }

    @Override
    public Version getLatestVersion() {
        return getAvailableVersions().stream()
                .filter(version -> version != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public Path getSoftwareVersion(Version version) {
        Folder folder = artifactory.repository(repositoryName).folder(pathToFolder).info();
        Item softwareItem = folder.getChildren().stream().filter(item -> version.versionEquals(getVersionFromName(item.getName()))).findFirst().orElse(null);
        if (softwareItem != null) {
            OutputStream outStream = null;
            try {
                Path tempPath = Paths.get(tempDirectory, softwareItem.getName());
                if (!Files.exists(tempPath)) {
                    // Only download if we don't already have this version
                    outStream = new FileOutputStream(tempPath.toFile());
                    log.info("Downloading " + version.getVersionString() + " to " + tempPath);
                    IOUtils.copy(artifactory.repository(repositoryName).download(pathToFolder + softwareItem.getUri()).doDownload(), outStream);
                }
                return tempPath;
            } catch (IOException exception) {
                log.error("Error saving to temp directory", exception);
            } finally {
                if (outStream != null)
                    try {
                        outStream.close();
                    } catch (IOException exception) {
                        log.error("Error closing Output stream", exception);
                    }
            }
        }
        return null;
    }

    private Version getVersionFromName(String name){
        Version resultingVersion = null;

        if (StringUtils.isNotEmpty(fileNamePattern)) {
            if (compiledFileNamePattern == null) {
                int flags = 0;

                if (fileNamePatternIgnoresCase) {
                    flags |= Pattern.CASE_INSENSITIVE;
                }

                compiledFileNamePattern = Pattern.compile(fileNamePattern, flags);
            }

            final Matcher matcher = compiledFileNamePattern.matcher(name);
            if (matcher.find()) {
                final String versionString = matcher.group("version");

                if (versionString != null) {
                    try {
                        resultingVersion = versionFactory.fromString(versionString);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } else {
            // assume the file name is named after the version number.
            resultingVersion = versionFactory.fromString(name);
        }

        return resultingVersion;
    }
}
