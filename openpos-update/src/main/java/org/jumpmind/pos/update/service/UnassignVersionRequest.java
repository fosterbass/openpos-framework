package org.jumpmind.pos.update.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnassignVersionRequest {
    int id;
    String groupId;
    String packageName;
}
