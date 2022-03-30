package org.jumpmind.pos.update.service;

import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.update.model.InstallGroupModel;
import org.jumpmind.pos.update.model.InstallRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Endpoint(path = "/update-mgt/createGroup")
public class CreateGroupEndpoint {
    @Autowired
    private InstallRepository repository;

    public CreateGroupResponse createGroup(CreateGroupRequest request) {
        final String newId = UUID.randomUUID().toString();

        final InstallGroupModel newGroup = InstallGroupModel.builder()
                .groupId(newId)
                .groupName(request.getName())
                .build();

        repository.saveGroup(newGroup);

        return CreateGroupResponse.builder()
                .groupId(newId)
                .build();
    }
}
