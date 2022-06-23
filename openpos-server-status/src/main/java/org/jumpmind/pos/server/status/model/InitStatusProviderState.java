package org.jumpmind.pos.server.status.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitStatusProviderState implements Serializable {
    String name;
    String currentState;
    String message;
}
