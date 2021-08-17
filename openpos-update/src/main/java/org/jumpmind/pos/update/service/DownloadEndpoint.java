package org.jumpmind.pos.update.service;

import org.apache.commons.io.IOUtils;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.model.InstallRepository;
import org.jumpmind.pos.update.provider.ISoftwareProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Profile(UpdateModule.NAME)
@Endpoint(path = DownloadEndpoint.PATH + "{version}/**")
public class DownloadEndpoint {

    final static String PATH = "/update/download/";

    @Autowired(required = false)
    Map<String, ISoftwareProvider> softwareProviders;

    @Value("${openpos.update.softwareProvider:fileSystemSoftwareProvider}")
    String softwareProvider;

    public void download(String version, HttpServletRequest request,
                         HttpServletResponse response) throws Exception {

        ISoftwareProvider provider = softwareProviders.get(softwareProvider);

        String path = (String)
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        path = path.substring(PATH.length()+version.length()+1);

        Path fromZip = provider.getSoftwareVersion(version);
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
