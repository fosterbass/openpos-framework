package org.jumpmind.pos.update.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.model.InstallGroupModel;
import org.jumpmind.pos.update.model.InstallRepository;
import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.jumpmind.pos.update.provider.SoftwareProviderFactory;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Profile(UpdateModule.NAME)
@Endpoint(path = "/update/installation/{installationId}")
public class UpdateEndpoint {

    @Autowired
    InstallRepository installRepository;

    @Value("${openpos.update.softwareProvider:fileSystemSoftwareProvider}")
    String softwareProvider;

    @Value("${openpos.update.installUrl}")
    String installUrl;

    @Value("${openpos.update.installBasePath}")
    String installBasePath;

    @Value("${openpos.update.requireAssigment:false}")
    boolean requireAssignment;

    Map<Version, String> versionToConfigXml = new ConcurrentHashMap<>();

    @Autowired
    Versioning versionFactory;

    @Autowired
    SoftwareProviderFactory softwareProviderFactory;

    ISoftwareProvider provider;

    @PostConstruct
    public void init(){
        provider = softwareProviderFactory.getSoftwareProvider();
    }

    public void update(String installationId, HttpServletResponse response) throws IOException {
        Version version = getExpectedVersion(installationId);

        if (version == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!versionToConfigXml.containsKey(version)) {
            Path fromZip = provider.getSoftwareVersion(version);

            if (fromZip != null) {
                Configuration configuration = buildConfiguration(version, fromZip);
                StringWriter writer = new StringWriter();
                configuration.write(writer);
                versionToConfigXml.put(version, writer.getBuffer().toString());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        response.getWriter().print(versionToConfigXml.get(version));
        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        response.flushBuffer();
    }

    private Version getExpectedVersion(String installationId) {
        Version version = null;
        InstallGroupModel installGroupModel = installRepository.findInstallGroup(installationId);

        if (installGroupModel != null) {
            if ("*".equals(installGroupModel.getTargetVersion())) {
                version = getLatestExpectedVersion();
            } else {
                try {
                    version = versionFactory.fromString(installGroupModel.getTargetVersion());
                } catch (IllegalArgumentException ex) {
                    log.error(
                            "could not parse version '{}' from the targeted install group '{}'; no update shall be provided",
                            installGroupModel.getTargetVersion(),
                            installGroupModel,
                            ex
                    );
                }
            }
        } else if (!requireAssignment) {
            version = getLatestExpectedVersion();
        }

        return version;
    }

    private Version getLatestExpectedVersion() {
        return provider.getLatestVersion();
    }

    private Configuration buildConfiguration(Version version, Path fromZip) throws IOException {
        Configuration.Builder configBuilder = Configuration.builder()
                .baseUri(installUrl + "/update/download/" + version.getVersionString() +"/")
                .basePath(installBasePath)
                .property("default.launcher.main.class", "org.jumpmind.pos.app.Commerce");

        try (FileSystem zipFs = FileSystems.newFileSystem(fromZip, ClassLoader.getSystemClassLoader())) {
            for(Path root : zipFs.getRootDirectories()) {
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String entry = file.toString();
                        configBuilder.file(
                                FileMetadata.readFrom(file)
                                        .uri(configBuilder.getBaseUri() + entry)
                                        .path(entry.substring(1))
                                        .ignoreBootConflict()
                                        .classpath(entry.endsWith("jar"))
                        );

                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            return configBuilder.build();
        }
    }
}
