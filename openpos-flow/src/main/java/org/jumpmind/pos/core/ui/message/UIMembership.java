package org.jumpmind.pos.core.ui.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UIMembership implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String nonMemberName;
    private boolean isMember;
}
