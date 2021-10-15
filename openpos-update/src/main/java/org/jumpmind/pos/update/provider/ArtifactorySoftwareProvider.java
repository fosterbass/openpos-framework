package org.jumpmind.pos.update.provider;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.lang3.StringUtils;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.ArtifactoryRequest;
import org.jfrog.artifactory.client.impl.ArtifactoryRequestImpl;
import org.jfrog.artifactory.client.model.Folder;
import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Profile(UpdateModule.NAME)
@Component
@Lazy
public class ArtifactorySoftwareProvider implements ISoftwareProvider{

    @Value("${openpos.update.artifactory.url}")
    String artifactoryUrl;
    
    @Value("${openpos.update.artifactory.repositoryName}")
    String repositoryName;
    
    @Value("{openpos.update.artifactory.pathToFolder}")
    String pathToFolder;

    @Value("${openpos.update.googleCloudStorageSoftwareProvider.namePattern}")
    String fileNamePattern;

    @Value("${openpos.update.fileSystemSoftwareProvider.namePatternIgnoreCase:false}")
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
        return folder.getChildren().stream().map(item -> getVersionFromFile(item.getName())).collect(Collectors.toList());
    }

    @Override
    public Version getLatestVersion() {
        return getAvailableVersions().stream().max(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public Path getSoftwareVersion(Version version) {
        return null;
    }

    private Version getVersionFromFile(String name){
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
