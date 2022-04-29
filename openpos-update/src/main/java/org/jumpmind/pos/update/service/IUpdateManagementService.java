package org.jumpmind.pos.update.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController("update-mgt")
@RequestMapping("/update-mgt")
@Tag(name="Auto Update Management Service")
public interface IUpdateManagementService {
    @RequestMapping(path = "/{package}/versions", method = RequestMethod.GET)
    GetAvailableVersionsResponse getAvailableVersions(
            @PathVariable(name = "package") String packageName,
            @RequestParam(required = false, name = "desc") Boolean orderDescending
    );

    @RequestMapping(path = "/groups", method = RequestMethod.GET)
    GetGroupsResponse getGroups(
            @RequestParam(required = false, name = "filter") String filter
    );

    @RequestMapping(path = "/groups/{id}", method = RequestMethod.GET)
    GetGroupResponse getGroup(@PathVariable(name = "id") String id);

    @RequestMapping(path = "/updateGroup", method = RequestMethod.PUT)
    UpdateGroupResponse updateGroup(@RequestBody UpdateGroupRequest request);

    @RequestMapping(path = "/getScheduledVersions", method = RequestMethod.PUT)
    GetScheduledVersionsResponse getScheduledVersions(@RequestBody GetScheduledVersionsRequest request);

    @RequestMapping(path = "/createGroup", method = RequestMethod.PUT)
    CreateGroupResponse createGroup(@RequestBody CreateGroupRequest request);

    @RequestMapping(path = "/removeGroup", method = RequestMethod.PUT)
    RemoveGroupResponse removeGroup(@RequestBody RemoveGroupRequest request);

    @RequestMapping(path = "/assign-version", method = RequestMethod.PUT)
    AssignVersionResponse assignVersion(@RequestBody AssignVersionRequest request);

    @RequestMapping(path = "/unassign-version", method = RequestMethod.PUT)
    UnassignVersionResponse unassignVersion(@RequestBody UnassignVersionRequest request);

    @RequestMapping(path = "/addMember", method = RequestMethod.PUT)
    AddMemberResponse addMember(@RequestBody AddMemberRequest request);

    @RequestMapping(path = "/removeMember", method = RequestMethod.PUT)
    RemoveMemberResponse removeMember(@RequestBody RemoveMemberRequest request);
}
