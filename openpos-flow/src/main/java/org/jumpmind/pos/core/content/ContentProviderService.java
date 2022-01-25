package org.jumpmind.pos.core.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Scope("device")
@Slf4j
public class ContentProviderService {

    @Autowired
    CacheManager cacheManager;

    @Value("${openpos.ui.content.providers:null}")
    String[] providers;

    @Autowired
    protected Map<String, IContentProvider> contentProviders;

    private static final String CACHE_NAME = "/content";

    public String resolveContent(String deviceId, String key) {
        List<IContentProvider> providerPriorities = getProviderPriorities();
        for (IContentProvider provider : providerPriorities) {
            String contentUrl = provider.getContentUrl(deviceId, key);
            if (contentUrl != null) {
                return contentUrl;
            }
        }

        return null;
    }

    private List<IContentProvider> getProviderPriorities() {
        List<IContentProvider> providerPriorities = new ArrayList<>();

        if (providers != null) {
            for (String provider : providers) {
                if (contentProviders.containsKey(provider)) {
                    providerPriorities.add(contentProviders.get(provider));
                }
            }
        }

        return providerPriorities;
    }

    public InputStream getContentInputStream(String contentPath, String provider) {
        IContentProvider contentProvider = contentProviders.get(provider);
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            String cacheKey = contentPath + ":" + provider;
            if (Objects.requireNonNull(cache).get(cacheKey, ByteArrayOutputStream.class) != null) {
                return new ByteArrayInputStream(Objects.requireNonNull(cache.get(cacheKey, ByteArrayOutputStream.class)).toByteArray());
            }
            InputStream contentInputStream = contentProvider.getContentInputStream(contentPath);
            if (contentInputStream != null) {
                ByteArrayOutputStream cacheValue = new ByteArrayOutputStream();
                copy(contentInputStream, cacheValue);
                Objects.requireNonNull(cache).putIfAbsent(cacheKey, cacheValue);
                return new ByteArrayInputStream(cacheValue.toByteArray());
            }
        } catch (IOException e) {
            log.debug("Unable to get content input stream", e);
        }
        log.debug("Resource not found for content: {}", contentPath);

        return null;
    }

    // TODO this can be replaced by InputStream.transferTo in Java 9
    private void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = source.read(buffer)) > -1) {
            target.write(buffer, 0, length);
        }
        target.flush();
        source.close();
    }
}
