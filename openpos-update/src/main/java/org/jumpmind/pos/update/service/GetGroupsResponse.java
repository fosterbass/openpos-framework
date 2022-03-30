package org.jumpmind.pos.update.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jumpmind.pos.update.model.InstallGroupModel;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupsResponse implements Serializable {
    private List<InstallGroupModel> groups;
}
