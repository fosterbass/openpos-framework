package org.jumpmind.pos.update.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallGroupModel;
import org.jumpmind.pos.update.model.InstallRepository;
import org.jumpmind.pos.update.provider.SoftwareProvider;
import org.jumpmind.pos.update.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Endpoint(path = "/update/manifest/{businessUnitId}/{package}")
public class ManifestEndpoint {

    @Autowired
    InstallRepository installRepository;

    @Value("${openpos.update.installUrl:set me}")
    String installUrl;

    @Value("${openpos.update.installBasePath:set me}")
    String installBasePath;

    @Value("${openpos.update.requireAssigment:false}")
    boolean requireAssignment;

    Map<Version, String> versionToConfigXml = new ConcurrentHashMap<>();

    @Autowired
    SoftwareProvider softwareProvider;

    void manifest(String businessUnitId, String packageName, HttpServletResponse response) throws IOException {
        Version version = getExpectedVersion(packageName, businessUnitId);

        if (version == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!versionToConfigXml.containsKey(version)) {
            Path fromZip = softwareProvider.getSoftwareProvider(packageName).getSoftwareVersion(version);

            if (fromZip != null) {
                Configuration configuration = buildConfiguration(packageName, version, fromZip);
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

    private Version getExpectedVersion(String packageName, String businessUnitId) {
        Version version = null;
        InstallGroupModel installGroupModel = installRepository.findInstallGroupForInstallation(businessUnitId);

        if (installGroupModel != null) {
            final String targetVersion = installGroupModel.getPackageVersions().get(packageName);
            if ("latest".equals(targetVersion)) {
                version = getLatestExpectedVersion(packageName);
            } else {
                try {
                    version = softwareProvider
                            .getVersioningFor(packageName)
                            .fromString(targetVersion);
                } catch (IllegalArgumentException ex) {
                    log.error(
                            "could not parse version '{}' of package '{}' from the targeted install group '{}'; no update shall be provided",
                            targetVersion,
                            packageName,
                            installGroupModel,
                            ex
                    );
                }
            }
        } else if (!requireAssignment) {
            version = getLatestExpectedVersion(packageName);
        }

        return version;
    }

    private Version getLatestExpectedVersion(String packageName) {
        return softwareProvider.getSoftwareProvider(packageName).getLatestVersion();
    }

    private Configuration buildConfiguration(String packageName, Version version, Path fromZip) throws IOException {
        Configuration.Builder configBuilder = Configuration.builder()
                .baseUri(installUrl + "/update/download/" + packageName + "/" + version.getVersionString() +"/")
                .basePath(installBasePath)

                // todo: this only works for commerce apps
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
