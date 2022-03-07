package org.jumpmind.pos.core.clientconfiguration;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ClientConfigurationSet implements Serializable {
    private Map<String, Map<String, String>> configsForTags;
}
