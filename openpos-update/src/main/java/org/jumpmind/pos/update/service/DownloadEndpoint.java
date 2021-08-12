package org.jumpmind.pos.update.service;

import org.apache.commons.io.IOUtils;
import org.jumpmind.pos.service.Endpoint;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Endpoint(path = DownloadEndpoint.PATH + "{version}/**")
public class DownloadEndpoint {

    final static String PATH = "/update/download/";

    public void download(String version, HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
        String path = (String)
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        path = path.substring(PATH.length()+version.length()+1);
        Path fromZip = Paths.get(new File("/Users/cshenso/Downloads/artifacts/aeo-commerce-" + version + ".zip").toURI());
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
