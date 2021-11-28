package org.jumpmind.pos.core.flow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "openpos.screens")
@Getter
@Setter
public class ScreensConfig {

    Map<String, List<String>> groupings;
    Map<String, ScreenConfig> config;

    public ScreenConfig findScreenConfig(String id) {
        ScreenConfig screenConfig = config.get(id);
        if (screenConfig == null && groupings != null) {
            for (Map.Entry<String, List<String>> entry : groupings.entrySet()) {
                if (entry.getValue().contains(id)) {
                    screenConfig = config.get(entry.getKey());
                    if (screenConfig != null) {
                        break;
                    }
                }
            }
        }

        if (screenConfig == null) {
            screenConfig = config.get("default");
        }

        return screenConfig;
    }

}
