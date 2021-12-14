package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.model.GetGroupsResponse;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestParam;

@Profile(UpdateModule.NAME)
@Endpoint(path = "/update-mgt/groups")
public class GetGroupsEndpoint {
    @Autowired
    InstallRepository repo;

    public GetGroupsResponse getGroups(@RequestParam(required = false, name = "filter") String filter) {
        return GetGroupsResponse.builder()
                .groups(repo.getInstallGroups(filter))
                .build();
    }
}
