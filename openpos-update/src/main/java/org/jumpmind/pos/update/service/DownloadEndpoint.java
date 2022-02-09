package org.jumpmind.pos.update.service;

import org.apache.commons.io.IOUtils;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.jumpmind.pos.update.provider.SoftwareProviderFactory;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Endpoint(path = DownloadEndpoint.PATH + "{version}/**")
public class DownloadEndpoint {
    static final String PATH = "/update/download/";

    @Autowired
    Versioning versionFactory;

    @Autowired
    SoftwareProviderFactory softwareProviderFactory;

    ISoftwareProvider provider;
    
    @PostConstruct
    public void init(){
        provider = softwareProviderFactory.getSoftwareProvider();
    }

    public void download(
            String version,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {

        if (version == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "version required");
            return;
        }

        final Version parsedVersion;
        try {
            parsedVersion = versionFactory.fromString(version);
        } catch (IllegalArgumentException ignored) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid version specified");
            return;
        }


        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        path = path.substring(PATH.length() + version.length() + 1);

        final Path fromZip = provider.getSoftwareVersion(parsedVersion);

        try (ZipFile zf = new ZipFile(fromZip.toFile())) {
            ZipEntry ze = zf.getEntry(path);
            if (ze != null) {
                try (InputStream is = zf.getInputStream(ze)) {
                    IOUtils.copy(is, response.getOutputStream());
                }
            }
        }

        response.flushBuffer();
    }
}
