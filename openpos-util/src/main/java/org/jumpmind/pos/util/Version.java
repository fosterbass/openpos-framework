package org.jumpmind.pos.util;

import lombok.Data;

@Data
public class Version {

    private String componentName;
    private String buildNumber;
    private String buildName;
    private String gitHash;
    private String gitBranch;
    private String version;
    private String buildTime;

}
