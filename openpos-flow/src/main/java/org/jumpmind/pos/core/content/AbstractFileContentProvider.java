package org.jumpmind.pos.core.content;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.core.flow.In;
import org.jumpmind.pos.core.flow.ScopeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.*;

@Scope("device")
@Slf4j
public abstract class AbstractFileContentProvider implements IContentProvider {

    public static final String SERVER_URL = "${apiServerBaseUrl}/appId/${appId}/deviceId/${deviceId}/content?contentPath=";

    public static final String PROVIDER_TOKEN = "&provider=";

    public static final String VERSION_TOKEN = "&version=";

    @Value("${openpos.ui.content.file.providerProperties:null}")
    String[] providerProperties;

    @Value("${openpos.ui.content.file.supportedFileTypes:null}")
    String[] supportedFileTypes;

    @Value("${openpos.ui.content.file.contentVersion:null}")
    protected String contentVersion;

    @In(scope = ScopeType.Device, required = false)
    Map<String, String> personalizationProperties;

    @In(scope = ScopeType.Device)
    IStateManager stateManager;

    static Map<String, ContentIndex> deviceContent = new HashMap<>();

    public String getMostSpecificContent(String deviceId, String key, String baseContentPath) {

        List<String> possibleContentDirs = getPossibleContentDirs(key);

        List<String> contentPaths = getMostSpecificContentPaths(baseContentPath, possibleContentDirs);

        String mostSpecific = null;
        if (CollectionUtils.isNotEmpty(contentPaths)) {
            int index = getDeviceIndex(deviceId, key, contentPaths.size());
            mostSpecific = contentPaths.get(index);
            log.debug("Found content for key: {}, content: {}", key, mostSpecific);
        } else {
            log.debug("No content found for key: {}", key);
        }

        return mostSpecific;
    }

    protected List<String> getMostSpecificContentPaths(String baseContentPath, List<String> possibleContentDirs) {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);

        for (String possibleContentDir : possibleContentDirs) {
            StringBuilder resourceBuilder = new StringBuilder(baseContentPath);
            if (!baseContentPath.endsWith("/")) {
                resourceBuilder.append("/");
            }
            resourceBuilder.append(possibleContentDir);
            resourceBuilder.append("/*");
            String resourcePath = resourceBuilder.toString();

            Resource[] resources;
            try {
                log.debug("Searching for content in path: {}", resourcePath);
                resources = resolver.getResources(resourcePath);

                List<String> files = new ArrayList<>();
                for (Resource resource : resources) {
                    if (isFileSupported(resource.getFilename())) {
                        StringBuilder relativePath = new StringBuilder(possibleContentDir)
                                .append("/")
                                .append(resource.getFilename());
                        files.add(relativePath.toString());
                    }
                }
                if (!files.isEmpty()) {
                    return files;
                }

            } catch (IOException e) {
                log.debug("Unable to find resource content", e);
            }
        }

        return Collections.emptyList();
    }

    protected List<String> getPossibleContentDirs(String key) {
        List<String> possibleContentDirs = new ArrayList<>();
        if (providerProperties != null && personalizationProperties != null) {
            for (int prop = providerProperties.length; prop >= 0; prop--) {
                StringBuilder builder = new StringBuilder(key);

                for (int i = 0; i < prop; i++) {
                    String property = providerProperties[i];
                    if (personalizationProperties.containsKey(property)) {
                        builder.append("/").append(personalizationProperties.get(property));
                    }
                }

                possibleContentDirs.add(builder.toString());
            }
        } else {
            possibleContentDirs.add(key);
        }

        return possibleContentDirs;
    }

    public boolean isFileSupported(String filename) {
        if (supportedFileTypes != null) {
            for (String fileType : supportedFileTypes) {
                if (filename.endsWith(fileType)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected int getDeviceIndex(String deviceId, String key, int size) {
        Integer index;
        ContentIndex contentIndex = deviceContent.get(deviceId);

        if (contentIndex == null) {
            contentIndex = new ContentIndex(key, (1 % size));
            deviceContent.put(deviceId, contentIndex);
            index = 0;
        } else {
            index = contentIndex.getIndex(key);
            if (index == null) {
                contentIndex.setIndex(key, (1 % size));
                index = 0;
            } else {
                contentIndex.setIndex(key, ((index + 1) % size));
                index %= size;
            }
        }

        return index;
    }

    protected String buildContentUrlToProvider(String provider, String contentPath) {
        StringBuilder urlBuilder = new StringBuilder(AbstractFileContentProvider.SERVER_URL);
        urlBuilder.append(contentPath);
        urlBuilder.append(PROVIDER_TOKEN);
        urlBuilder.append(provider);
        if (contentVersion != null) {
            urlBuilder.append(VERSION_TOKEN);
            urlBuilder.append(contentVersion);
        }
        return urlBuilder.toString();
    }

    public static class ContentIndex {
        private final Map<String, Integer> indexes;

        private ContentIndex() {
            indexes = new HashMap<>();
        }

        private ContentIndex(String resource, int index) {
            indexes = new HashMap<>();
            indexes.put(resource, index);
        }

        private void setIndex(String key, int index) {
            indexes.put(key, index);
        }

        private Integer getIndex(String key) {
            return indexes.get(key);
        }
    }

}
