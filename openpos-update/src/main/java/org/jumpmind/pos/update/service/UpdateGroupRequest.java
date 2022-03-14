package org.jumpmind.pos.update.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequest {
    String groupId;

    /**
     * If not null, the name is updated to the specified value.
     */
    String name;
}
