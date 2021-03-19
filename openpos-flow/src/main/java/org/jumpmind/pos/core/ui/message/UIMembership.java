package org.jumpmind.pos.core.ui.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class UIMembership implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private boolean isMember;
}
