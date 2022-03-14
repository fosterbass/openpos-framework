package org.jumpmind.pos.update.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallGroupModel;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Endpoint(path = "/update-mgt/updateGroup")
public class UpdateGroupEndpoint {
    @Autowired
    InstallRepository repository;

    public UpdateGroupResponse updateGroup(UpdateGroupRequest request) {
        if (request.getGroupId() == null) {
            log.warn("request to to update group but missing required group id in the request");
            return failureResponse();
        }

        final InstallGroupModel group = repository.findInstallGroup(request.getGroupId());
        if (group == null) {
            return failureResponse();
        }

        if (request.getName() != null) {
            group.setGroupName(request.getName());
        }

        try {
            repository.save(group);
        } catch (Exception ex) {
            log.error("failed to save changes to group", ex);
            return failureResponse();
        }

        return UpdateGroupResponse.builder().success(true).build();
    }

    public UpdateGroupResponse failureResponse() {
        return UpdateGroupResponse.builder().success(false).build();
    }
}
