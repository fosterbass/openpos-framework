package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.model.InstallGroupModel;
import org.jumpmind.pos.update.model.InstallRepository;
import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Profile(UpdateModule.NAME)
@Endpoint(path = "/update/installation/{installationId}")
public class UpdateEndpoint {

    @Autowired(required = false)
    Map<String, ISoftwareProvider> softwareProviders;

    @Autowired
    InstallRepository installRepository;

    @Value("${openpos.update.softwareProvider:fileSystemSoftwareProvider}")
    String softwareProvider;

    @Value("${openpos.update.installUrl}")
    String installUrl;

    @Value("${openpos.update.installBasePath}")
    String installBasePath;

    Map<Version, String> versionToConfigXml = new ConcurrentHashMap<>();

    @Autowired
    Versioning versionFactory;

    public void update(
            String installationId,
            HttpServletResponse response
    ) throws Exception {
        ISoftwareProvider provider = softwareProviders.get(softwareProvider);

        Version version = null;
        InstallGroupModel installGroupModel = installRepository.findInstallGroup(installationId);

        if (installGroupModel != null) {
            if ("*".equals(installGroupModel.getTargetVersion())) {
                version = provider.getLatestVersion();
            } else {
                try {
                    version = versionFactory.fromString(installGroupModel.getTargetVersion());
                } catch (IllegalArgumentException ignored) {
                    response.sendError(HttpServletResponse.SC_CONFLICT, "server has invalid version for target group");
                    return;
                }
            }
        } else {
            version = provider.getLatestVersion();
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
        response.flushBuffer();
    }

    Configuration buildConfiguration(Version version, Path fromZip) throws IOException {
        Configuration.Builder configBuilder = Configuration.builder()
                .baseUri(installUrl + "/update/download/" + version.getVersionString() +"/")
                .basePath(installBasePath)
                .property("default.launcher.main.class", "org.jumpmind.pos.app.Commerce");

        FileSystem zipFs = FileSystems.newFileSystem(fromZip, ClassLoader.getSystemClassLoader());

        for(Path root : zipFs.getRootDirectories()) {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    String entry = file.toString();
                    configBuilder.file(FileMetadata.readFrom(file).
                            uri(configBuilder.getBaseUri() + entry).
                            path(entry.substring(1)).ignoreBootConflict().
                            classpath(entry.endsWith("jar")));
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return configBuilder.build();
    }
}
