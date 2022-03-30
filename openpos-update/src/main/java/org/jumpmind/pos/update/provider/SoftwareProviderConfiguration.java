package org.jumpmind.pos.update.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareProviderConfiguration<T> {
    public String provider = "filesystem";
    public String versioning = "semver";
    public T config;
}
