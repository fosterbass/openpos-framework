package org.jumpmind.pos.update.service;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallGroupModel;
import org.jumpmind.pos.update.model.InstallRepository;
import org.jumpmind.pos.update.model.TargetVersionModel;
import org.jumpmind.pos.util.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Endpoint(path = "/update-mgt/getScheduledVersions")
public class GetScheduledVersionsEndpoint {
    @Autowired
    InstallRepository repo;

    public GetScheduledVersionsResponse getScheduledVersions(GetScheduledVersionsRequest request) {
        boolean isGroupIdSpecified = StringUtils.isNotEmpty(request.getGroupId());
        boolean isBusinessUnitIdSpecified = StringUtils.isNotEmpty(request.getBusinessUnitId());

        if (isGroupIdSpecified && isBusinessUnitIdSpecified) {
            throw new IllegalArgumentException("request cannot include both a group id and installation id");
        }

        if (!isGroupIdSpecified && !isBusinessUnitIdSpecified) {
            throw new IllegalArgumentException("request must have either a group id or installation id");
        }

        final String groupId;

        if (isBusinessUnitIdSpecified) {
            final InstallGroupModel groupModel = repo.findInstallGroupForInstallation(request.getBusinessUnitId());

            if (groupModel == null) {
                throw new NotFoundException();
            }

            groupId = groupModel.getGroupId();
        } else {
            groupId = request.getGroupId();
        }

        final List<TargetVersionModel> versions = repo.getAllScheduledGroupVersions(groupId, request.getPackageName());

        return GetScheduledVersionsResponse.builder()
                .scheduledVersions(versions)
                .build();
    }
}
