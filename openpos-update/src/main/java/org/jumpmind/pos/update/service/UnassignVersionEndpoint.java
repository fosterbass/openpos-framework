package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;


@Endpoint(path = "/update-mgt/unassign-version")
public class UnassignVersionEndpoint {
    @Autowired
    InstallRepository repository;

    public UnassignVersionResponse unassignVersion(@RequestBody UnassignVersionRequest request) {
        repository.removeTargetVersion(request.getId(), request.getGroupId(), request.getPackageName());
        return UnassignVersionResponse.builder().success(true).build();
    }
}
