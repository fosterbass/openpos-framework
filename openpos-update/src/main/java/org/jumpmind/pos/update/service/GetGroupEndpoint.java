package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Endpoint(path = "/update-mgt/groups/{id}")
public class GetGroupEndpoint {
    @Autowired
    InstallRepository repo;

    public GetGroupResponse getGroup(String id) {
        return GetGroupResponse.builder()
                .group(repo.findInstallGroup(id))
                .members(repo.getMembersOfGroup(id))
                .build();
    }
}
