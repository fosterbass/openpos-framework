package org.jumpmind.pos.update.provider.artifactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.model.Folder;
import org.jfrog.artifactory.client.model.Item;
import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.jumpmind.pos.update.provider.NamedFilesSoftwareProvider;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.jumpmind.pos.update.versioning.Version;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ArtifactorySoftwareProvider extends NamedFilesSoftwareProvider {
    String artifactoryUrl;
    String repositoryName;
    String pathToFolder;

    Artifactory artifactory;

    private String tempDirectory;

    public ArtifactorySoftwareProvider(PackageVersioning versioning, ArtifactorySoftwareProviderConfiguration config) {
        super(config.getFileNamePattern(), config.isFileNamePatternIgnoresCase(), versioning);

        artifactoryUrl = config.getUrl();
        repositoryName = config.getRepository();
        pathToFolder = config.getDirectoryPath();
    }

    @PostConstruct
    public void init() {
        
        try {
            tempDirectory = Files.createTempDirectory("jmcUpdates").toFile().getAbsolutePath();
        } catch (IOException e) {
            log.error("could not create temporary storage for artifactory downloads", e);
        }

        artifactory = ArtifactoryClientBuilder.create()
                .setUrl(artifactoryUrl)
                .build();
    }
    
    @Override
    public List<Version> getAvailableVersions() {
        Folder folder = artifactory.repository(repositoryName).folder(pathToFolder).info();
        return folder.getChildren().stream().map(item -> getVersionOfFile(item.getName())).collect(Collectors.toList());
    }

    @Override
    public Version getLatestVersion() {
        return getAvailableVersions().stream()
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public Path getSoftwareVersion(Version version) {
        Folder folder = artifactory.repository(repositoryName).folder(pathToFolder).info();
        Item softwareItem = folder.getChildren().stream()
                .filter(item -> version.versionEquals(getVersionOfFile(item.getName())))
                .findFirst()
                .orElse(null);

        if (softwareItem != null) {
            Path tempPath = Paths.get(tempDirectory, softwareItem.getName());

            if (!Files.exists(tempPath)) {
                try (final OutputStream outStream = new FileOutputStream(tempPath.toFile())) {
                    log.info("Downloading " + version.getVersionString() + " to " + tempPath);
                    IOUtils.copy(
                            artifactory
                                .repository(repositoryName)
                                .download(pathToFolder + softwareItem.getUri()).doDownload(), outStream
                    );
                } catch (IOException exception) {
                    log.error("Error saving to temp directory", exception);
                }
            }

            return tempPath;
        }

        return null;
    }
}
