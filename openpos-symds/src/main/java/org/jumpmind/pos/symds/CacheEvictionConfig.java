package org.jumpmind.pos.symds;

import lombok.Data;
import org.jumpmind.util.LinkedCaseInsensitiveMap;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "openpos.symmetric.cache-eviction-config")
public class CacheEvictionConfig {
    private LinkedCaseInsensitiveMap<List<String>> tables;
    private LinkedCaseInsensitiveMap<List<String>> channels;

}
