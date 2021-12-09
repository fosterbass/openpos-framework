package org.jumpmind.pos.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.apache.commons.lang3.StringUtils.*;

@Component
public class Versions {

    static List<Version> versions = new ArrayList<>();

    static final Logger log = LoggerFactory.getLogger(Versions.class);

    @Value("${openpos.general.defaultVersionComponent:jmc}")
    String defaultVersionComponent;

    List<InputStream> loadResources(final String name) throws IOException {
        final List<InputStream> list = new ArrayList<InputStream>();
        Resource[] resources = ResourceUtils.getResources("openpos-version.properties");
        if (resources != null) {
            for (Resource resource : resources) {
                list.add(resource.getInputStream());
            }
        }
        return list;
    }

    @PostConstruct
    protected synchronized void init() {
        try {
            List<Version> building = new ArrayList<>();
            List<InputStream> resources = loadResources("openpos-version.properties");
            log.info(BoxLogging.box("Versions"));
            for (InputStream is : resources) {
                Properties properties = new Properties();
                properties.load(is);
                ObjectMapper m = DefaultObjectMapper.build();
                Version version = m.convertValue(properties, Version.class);
                log.info(m.writerWithDefaultPrettyPrinter().writeValueAsString(version));
                building.add(version);
            }
            versions = building;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<Version> getVersions() {
        return versions;
    }

    @Override
    public String toString() {
        return versions();
    }

    public static String versions() {
        StringBuilder b = new StringBuilder();
        if (versions.size() > 0) {
            b.append("\n*******************************************************\n");
            b.append("Versions:\n");
            for (Version version : versions) {
                b.append(version.toString()).append("\n");
            }
            b.append("*******************************************************\n");
        }
        return b.toString();
    }

    public int getFormattedVersion() {
        String version = versions.stream().filter(v -> v.getComponentName().equals(defaultVersionComponent)).
                findFirst().orElse(new Version()).getVersion();
        return formatVersion(version);
    }

    protected int formatVersion(String version) {
        if (version != null && !"@version@".equals(version)) {
            final String SNAPSHOT = "-SNAPSHOT";
            StringBuilder formattedVersion = new StringBuilder();
            if (version.endsWith(SNAPSHOT)) {
                version = version.substring(0, version.length() - SNAPSHOT.length());
            }
            for (int i = 0; i < 3; i++) {
                if (version.indexOf(".") >= 0) {
                    String part = version.substring(0, version.indexOf("."));
                    formattedVersion.append(truncate(leftPad(part, 3, '0'), 3));
                    version = version.substring(version.indexOf(".") + 1);
                } else if (version.length() > 0) {
                    formattedVersion.append(leftPad(truncate(version, 3), 3, '0'));
                    version = "";
                } else {
                    formattedVersion.append("000");
                }
            }
            return Integer.parseInt(formattedVersion.toString());
        } else {
            return 0;
        }
    }
}
