package org.jumpmind.pos.server.status.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerInitStatusResponse implements Serializable {
    List<InitStatusProviderState> providers;
}
