package org.jumpmind.pos.service.groovyconsole;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openpos.developer.groovy-console")
@Data
public final class GroovyConsoleConfig {
    private boolean enabled = false;
}
