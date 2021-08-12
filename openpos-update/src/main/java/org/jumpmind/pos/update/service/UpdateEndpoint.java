package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

@Endpoint(path = "/update/installation/{installationId}")
public class UpdateEndpoint {

    public void update(String installationId,
                       HttpServletResponse response) throws Exception {
        final String version = "4.0.0.461";
        // TODO repository to configure versions

        // TODO pluggable file provider api
        // TODO regexp to get versions
        Path fromZip = Paths.get(new File("/Users/cshenso/Downloads/artifacts/aeo-commerce-" + version + ".zip").toURI());

        // TODO make base uri and path configurable
        Configuration.Builder configBuilder = Configuration.builder()
                .baseUri("http://localhost:6142/update/download/" + version +"/")
                .basePath("${user.dir}")
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

        configBuilder.build().write(response.getWriter());
        response.flushBuffer();
    }
}
