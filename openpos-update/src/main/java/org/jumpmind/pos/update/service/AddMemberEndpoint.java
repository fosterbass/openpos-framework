package org.jumpmind.pos.update.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Endpoint(path = "/update-mgt/addMember")
public class AddMemberEndpoint {
    @Autowired
    InstallRepository repo;

    public AddMemberResponse addMember(AddMemberRequest request) {
        boolean success = true;

        try {
            repo.addMemberToGroup(request.getGroupId(), request.getBusinessUnitId());
        } catch (Exception ex) {
            log.error("failed to add business unit '{}' to group '{}'",
                    request.getBusinessUnitId(),
                    request.getGroupId(),
                    ex);

            success = false;
        }

        return AddMemberResponse.builder().success(success).build();
    }
}
