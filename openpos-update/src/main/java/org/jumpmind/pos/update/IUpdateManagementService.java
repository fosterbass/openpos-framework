package org.jumpmind.pos.update;

import org.jumpmind.pos.update.model.GetAvailableVersionsResponse;
import org.jumpmind.pos.update.model.GetGroupsResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("update-mgt")
@RequestMapping("/update-mgt")
public interface IUpdateManagementService {
    @RequestMapping(path = "/versions", method = RequestMethod.GET)
    GetAvailableVersionsResponse getAvailableVersions(
            @RequestParam(required = false, name = "desc") Boolean orderDescending
    );

    @RequestMapping(path = "/groups", method = RequestMethod.GET)
    GetGroupsResponse getGroups(
            @RequestParam(required = false, name = "filter") String filter
    );
}
