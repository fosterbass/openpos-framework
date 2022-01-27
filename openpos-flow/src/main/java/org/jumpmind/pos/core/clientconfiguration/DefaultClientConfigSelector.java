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
    private List<ClientConfigurationSet> clientConfigSets = new ArrayList<>();
    private Map<String, Map<String, String>> defaultConfigs = new HashMap<>();
    String defaultTag = "default";

    @Override
    public Map<String, Map<String, String>> getConfigurations(Map<String, String> properties, List<String> additionalTags) {

        Map<String, Map<String, String>> configurations = new HashMap<>();
        List<List<String>> tagGroups = new ArrayList<>();
        List<String> tagsForSpecificity = new ArrayList<>();

        if (additionalTags != null) {
            // Pass along all the additional tags
            tagsForSpecificity.addAll(additionalTags);
        }

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
        sortDefaultFirst(tagsForSpecificity);
        // Start with default
        tagGroups.add(Arrays.asList(defaultTag));

        List<List<String>> uniquePermutations = uniqueTagGroupCombinations(tagsForSpecificity);
        // sort so that they are in order of the least specificity to most specificity ("a" comes before "a, b, c")
        uniquePermutations.sort(Comparator.comparingInt(List::size));
        tagGroups.addAll(uniquePermutations);

        Map<List<String>, Map<String, Map<String, String>>> clientConfigsByTagsAndName = new HashMap<>();

        clientConfigsByTagsAndName.put(Arrays.asList("default"), defaultConfigs);

        clientConfigSets.forEach(clientConfigurationSet -> {
            clientConfigurationSet.getTags().sort(String::compareTo);
            clientConfigsByTagsAndName.put(clientConfigurationSet.getTags(), clientConfigurationSet.getConfigsForTags());
        });

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

    public static List<List<String>> uniqueTagGroupCombinations(List<String> tags) {
        List<List<String>> results = new ArrayList<>();
        for (String tag : tags) {
            int resultsLength = results.size();
            for (int j = 0; j < resultsLength; j++) {
                List<String> newList = new ArrayList<>(results.get(j));
                newList.add(tag);
                results.add(newList);
            }
            results.add(Arrays.asList(tag));
        }
        return results;
    }
}
