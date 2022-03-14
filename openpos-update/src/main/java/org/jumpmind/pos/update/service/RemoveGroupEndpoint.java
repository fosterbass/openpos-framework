package org.jumpmind.pos.update.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Endpoint(path = "/update-mgt/removeGroup")
public class RemoveGroupEndpoint {
    @Autowired
    InstallRepository repo;

    @Autowired
    IUpdateManagementService managementService;

    public RemoveGroupResponse removeGroup(RemoveGroupRequest request) {
        final GetGroupResponse response = managementService.getGroup(request.getGroupId());

        try {
            repo.removeGroup(response.getGroup());
        } catch (Exception ex) {
            log.error(
                    "failed to remove group '{}'",
                    response.getGroup().getGroupId(),
                    ex
            );

            return makeResponse(false);
        }

        return makeResponse(true);
    }

    private RemoveGroupResponse makeResponse(boolean success) {
        return RemoveGroupResponse.builder().success(success).build();
    }
}
