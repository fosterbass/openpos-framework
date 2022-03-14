package org.jumpmind.pos.update.service;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallGroupMemberModel;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Endpoint(path = "/update-mgt/removeMember")
public class RemoveMemberEndpoint {
    @Autowired
    InstallRepository repo;

    public RemoveMemberResponse removeMember(RemoveMemberRequest request) {
        final InstallGroupMemberModel member;
        try {
            member = repo.getGroupMember(request.getGroupId(), request.getBusinessUnitId());
            if (member == null) {
                return makeResponse(false);
            }
        } catch (Exception ex) {
            log.error("failed to locate group member '{}' for group '{}'", request.getBusinessUnitId(), request.getGroupId(), ex);
            return makeResponse(false);
        }

        try {
            repo.removeGroupMember(member);
        } catch (Exception ex) {
            log.error("failed to remove group member '{}' for group '{}'", request.getBusinessUnitId(), request.getGroupId(), ex);
            return makeResponse(false);
        }

        return makeResponse(true);
    }

    private RemoveMemberResponse makeResponse(boolean success) {
        return RemoveMemberResponse.builder().success(success).build();
    }
}
