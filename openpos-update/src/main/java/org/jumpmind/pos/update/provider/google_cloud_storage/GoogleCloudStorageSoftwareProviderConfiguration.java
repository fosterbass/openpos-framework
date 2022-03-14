package org.jumpmind.pos.update.provider.google_cloud_storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.update.provider.SoftwareProviderConfigurationDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCloudStorageSoftwareProviderConfiguration {
    public String bucketName;
    public String fileNamePattern = SoftwareProviderConfigurationDefaults.FILE_NAME_PATTERN;
    public boolean fileNamePatternIgnoreCase = SoftwareProviderConfigurationDefaults.FILE_NAME_PATTERN_IGNORE_CASE;
}
