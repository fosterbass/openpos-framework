package org.jumpmind.pos.core.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component("fileSystemContentProvider")
@Scope("device")
public class FileSystemContentProvider extends AbstractFileContentProvider {

    @Value("${openpos.ui.content.file-system.baseContentPath:content/}")
    String baseContentPath;

    @Override
    public String getContentUrl(String deviceId, String key) {
        String filePathContent = "file:" + baseContentPath;
        String contentPath = getMostSpecificContent(deviceId, key, filePathContent);

        if (contentPath != null) {
            return buildContentUrlToProvider("fileSystemContentProvider", contentPath);
        }

        return null;
    }

    @Override
    public InputStream getContentInputStream(String contentPath) throws IOException {
        if (isFileSupported(contentPath)) {
            StringBuilder pathBuilder = new StringBuilder("file:");
            if (!contentPath.contains(baseContentPath)) {
                pathBuilder.append(baseContentPath);
            }
            pathBuilder.append(contentPath);
            String filePathContent = pathBuilder.toString();

            ClassLoader cl = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = resolver.getResources(filePathContent);

            if (resources.length > 0) {
                return resources[0].getInputStream();
            }
        }

        return null;
    }
}
