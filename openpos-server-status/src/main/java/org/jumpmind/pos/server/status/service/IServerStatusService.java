package org.jumpmind.pos.server.status.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.jumpmind.pos.server.status.model.ServerInitStatusResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Status Service", description = "Exposes endpoints to retrieve status information generally about the server itself")
@RestController("status")
@RequestMapping("/status")
public interface IServerStatusService {

    @ResponseBody
    @GetMapping(path = "/init")
    ServerInitStatusResponse init();
}
