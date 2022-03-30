package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.provider.SoftwareProvider;
import org.jumpmind.pos.update.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Endpoint(path = "/update-mgt/{package}/versions")
public class GetAvailableVersionsEndpoint {
    @Autowired
    SoftwareProvider provider;

    public GetAvailableVersionsResponse getAvailableVersions(
            String packageName,
            Boolean desc
    ) {
        final boolean sortDescending = desc != null && desc;

        final List<String> versions = provider
                .getSoftwareProvider(packageName)

                // todo: software provider doesn't exist?
                .getAvailableVersions()
                .stream()
                .sorted(sortDescending ? Comparator.reverseOrder() : Comparator.naturalOrder())
                .map(Version::getVersionString)
                .collect(Collectors.toList());

        return GetAvailableVersionsResponse.builder()
                .versions(versions)
                .build();
    }
}
