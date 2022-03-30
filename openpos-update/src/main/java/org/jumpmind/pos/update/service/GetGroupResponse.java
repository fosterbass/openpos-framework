package org.jumpmind.pos.update.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.update.model.InstallGroupMemberModel;
import org.jumpmind.pos.update.model.InstallGroupModel;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetGroupResponse {
    InstallGroupModel group;
    List<InstallGroupMemberModel> members;
}
