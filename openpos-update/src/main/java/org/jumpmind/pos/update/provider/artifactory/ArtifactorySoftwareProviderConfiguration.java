package org.jumpmind.pos.update.provider.artifactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.update.provider.SoftwareProviderConfigurationDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactorySoftwareProviderConfiguration {
    String url;
    String repository;
    String directoryPath;
    String fileNamePattern = SoftwareProviderConfigurationDefaults.FILE_NAME_PATTERN;
    boolean fileNamePatternIgnoresCase = SoftwareProviderConfigurationDefaults.FILE_NAME_PATTERN_IGNORE_CASE;
}
