package org.jumpmind.pos.update.provider.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSystemSoftwareProviderConfiguration {
    public String artifactType = "zip";
    public String baseDirectory = "/";
    public String fileNamePattern = "^(\\w+-)+(?<version>(\\d+)(\\.(\\d+)(\\.(\\d+))?)?(-+([\\w-]+[\\w\\-.]+))?(\\+([\\w-]+[\\w\\-.]+))?)\\.zip$";
    public boolean fileNamePatternIgnoreCase = false;
}
