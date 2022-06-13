package org.jumpmind.pos.server.status.service;

import org.jumpmind.pos.server.status.model.InitStatusProviderState;
import org.jumpmind.pos.server.status.model.ServerInitStatusResponse;
import org.jumpmind.pos.service.Endpoint;
import org.jumpmind.pos.service.init.IModuleStatusProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Endpoint(path = "/status/init")
public class ServerStatusInitEndpoint {
    @Autowired
    public List<IModuleStatusProvider> initProviders;

    public ServerInitStatusResponse init() {
        return ServerInitStatusResponse.builder()
                .providers(
                        initProviders.stream()
                                .map(p ->
                                        InitStatusProviderState.builder()
                                                .name(p.getDisplayName())
                                                .currentState(p.getCurrentStatus().getStatus().name())
                                                .message(p.getCurrentStatus().getMessage())
                                                .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }
}
