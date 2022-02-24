package org.jumpmind.pos.core.clientconfiguration;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConfigurationProperties(prefix = "openpos.client-configuration")
@Scope("prototype")
@Data
public class DefaultClientConfigSelector implements IClientConfigSelector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> propertiesForTags = new ArrayList<>();
    private Map<String, ClientConfigurationSet> clientConfigSets = new HashMap<>();
    private Map<String, Map<String, String>> defaultConfigs = new HashMap<>();
    String defaultTag = "default";

    @Override
    public Map<String, Map<String, String>> getConfigurations(Map<String, String> properties, List<String> additionalTags) {

        Map<String, Map<String, String>> configurations = new HashMap<>();
        Set<String> tagGroups = new LinkedHashSet<>();
        List<String> tagsForSpecificity = new ArrayList<>();

        // Lookup the values for our tags
        propertiesForTags.forEach(s -> {
            if (properties.containsKey(s)) {
                String value = properties.get(s);
                if (value != null) {
                    tagsForSpecificity.add(value);
                }
            } else {
                logger.info("Could not find personalization parameter {}", s);
            }
        });
        if (additionalTags != null) {
            // Pass along all the additional tags
            tagsForSpecificity.addAll(additionalTags);
        }

        sortDefaultFirst(tagsForSpecificity);
        // Start with default
        tagGroups.add(defaultTag);
        tagGroups.addAll(tagsForSpecificity);

        Map<String, Map<String, Map<String, String>>> clientConfigsByTagsAndName = new HashMap<>();

        clientConfigsByTagsAndName.put("default", defaultConfigs);

        clientConfigSets.forEach((key, clientConfigurationSet) -> clientConfigsByTagsAndName.put(key, clientConfigurationSet.getConfigsForTags()));

        tagGroups.forEach(tags -> {
            if (clientConfigsByTagsAndName.containsKey(tags) && clientConfigsByTagsAndName.get(tags) != null) {
                configurations.putAll(clientConfigsByTagsAndName.get(tags));
            }
        });

        return configurations;
    }

    private void sortDefaultFirst(List<String> tagsForSpecificity) {
        tagsForSpecificity.sort((o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            } else if ((o1 == null || o1.equals(defaultTag)) && (o2 != null && !o2.equals(defaultTag))) {
                return -1;
            } else if ((o1 != null && !o1.equals(defaultTag)) && (o2 == null || o2.equals(defaultTag))) {
                return 1;
            }
            return 0;
        });
    }
}
