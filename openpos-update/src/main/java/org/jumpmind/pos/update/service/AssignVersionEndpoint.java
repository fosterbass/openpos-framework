package org.jumpmind.pos.update.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Endpoint(path = "/update-mgt/assign-version")
public class AssignVersionEndpoint {
    @Autowired
    InstallRepository repo;

    public AssignVersionResponse assignVersion(AssignVersionRequest request) {
        boolean success = true;

        try {
            repo.scheduleVersion(
                    request.getGroupId(),
                    request.getPackageName(),
                    request.getVersion(),
                    request.getEffectiveDate()
            );
        } catch (Exception ex) {
            log.error("unknown exception occurred while attempting to assign {} version '{}' to group '{}'",
                    request.getPackageName(),
                    request.getVersion(),
                    request.getGroupId(),
                    ex);
            success = false;
        }


        return AssignVersionResponse.builder().success(success).build();
    }
}
