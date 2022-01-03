package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.model.GetAvailableVersionsResponse;
import org.jumpmind.pos.update.provider.SoftwareProviderFactory;
import org.jumpmind.pos.update.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Endpoint(path = "/update-mgt/versions")
public class GetAvailableVersionsEndpoint {
    @Autowired
    SoftwareProviderFactory provider;

    public GetAvailableVersionsResponse getAvailableVersions(@RequestParam(required = false, name = "desc") Boolean desc) {
        final boolean sortDescending = desc != null && desc;

        final List<String> versions = provider.getSoftwareProvider()
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
