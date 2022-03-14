package org.jumpmind.pos.update.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.persist.Query;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InstallRepository {

    @Autowired
    @Qualifier("updateSession")
    DBSession dbSession;

    @Autowired
    Versioning versioning;

    private final Query<InstallGroupModel> findGroupsQuery = new Query<InstallGroupModel>()
            .named("selectGroupsWithFilter")
            .result(InstallGroupModel.class);

    private final Query<InstallGroupModel> findGroupForInstallation = new Query<InstallGroupModel>()
            .named("findGroupForBusinessUnit")
            .result(InstallGroupModel.class);

    private final Query<TargetVersionModel> getAllVersionsOfGroupQuery = new Query<TargetVersionModel>()
            .named("getAllVersionsOfGroup")
            .result(TargetVersionModel.class);

    private final Query<TargetVersionModel> getGroupVersionHistory = new Query<TargetVersionModel>()
            .named("getVersionHistoryOfGroup")
            .result(TargetVersionModel.class);

    private final Query<TargetVersionModel> getScheduledUpdatesForGroupQuery = new Query<TargetVersionModel>()
            .named("getScheduledVersionsOfGroup")
            .result(TargetVersionModel.class);

    private final Query<InstallGroupMemberModel> getGroupMembersQuery = new Query<InstallGroupMemberModel>()
            .named("getGroupMembers")
            .result(InstallGroupMemberModel.class);

    private final Query<InstallGroupMemberModel> findGroupMemberQuery = new Query<InstallGroupMemberModel>()
            .named("findGroupMember")
            .result(InstallGroupMemberModel.class);

    private static final String GROUP_ID_PARAM = "groupId";
    private static final String PACKAGE_PARAM = "package";
    private static final String NOW_PARAM = "now";

    public TargetVersionModel getTargetVersion(String groupId, String packageName) {
        final Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID_PARAM, groupId);
        params.put(PACKAGE_PARAM, packageName);
        params.put(NOW_PARAM, new Date());

        return dbSession.query(getGroupVersionHistory, params, 10000)
                .stream()
                .filter(v -> this.isValidVersionString(v.getPackageName(), v.getVersion()))
                .findFirst()
                .orElse(null);
    }

    public Map<String, TargetVersionModel> getAllTargetVersions(String groupId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID_PARAM, groupId);
        params.put(NOW_PARAM, new Date());

        return dbSession.query(getGroupVersionHistory, params, 10000)
                .stream()
                .filter(v -> this.isValidVersionString(v.getPackageName(), v.getVersion()))
                .collect(
                        Collectors.toMap(
                                TargetVersionModel::getPackageName,
                                x -> x,

                                // choose the latest assigned version.
                                (x, y) -> x
                        )
                );
    }

    public InstallGroupModel findInstallGroup(String groupId) {
        return coerceWithTargetVersion(dbSession.findByNaturalId(InstallGroupModel.class, groupId));
    }

    public InstallGroupModel findInstallGroupForInstallation(String businessUnitId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("businessUnitId", businessUnitId);
        params.put(NOW_PARAM, new Date());

        return dbSession.query(findGroupForInstallation, params, 1)
                .stream()
                .findFirst()
                .map(this::coerceWithTargetVersion)
                .orElse(null);
    }

    public void save(InstallGroupModel group) {
        dbSession.save(group);
    }

    public List<InstallGroupModel> getInstallGroups() {
        return getInstallGroups(null);
    }

    public List<InstallGroupModel> getInstallGroups(String filter) {
        final Map<String, Object> params = new HashMap<>();
        params.put(NOW_PARAM, new Date());

        if (StringUtils.isNotEmpty(filter)) {
            params.put("filter", "%" + filter + "%");
        }

        return dbSession.query(findGroupsQuery, params,10000)
                .stream()
                .map(this::coerceWithTargetVersion)
                .collect(Collectors.toList());
    }

    public List<TargetVersionModel> getAllGroupVersions(InstallGroupModel installGroup) {
        return getAllGroupVersions(installGroup.getGroupId());
    }

    private List<TargetVersionModel> getAllGroupVersions(InstallGroupModel installGroup, boolean includeInvalid) {
        return getAllGroupVersions(installGroup.getGroupId(), includeInvalid);
    }

    public List<TargetVersionModel> getAllGroupVersions(String groupId) {
        return getAllGroupVersions(groupId, false);
    }

    private List<TargetVersionModel> getAllGroupVersions(String groupId, boolean includeInvalid) {
        final Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID_PARAM, groupId);
        params.put(NOW_PARAM, new Date());

        List<TargetVersionModel> result = dbSession.query(getAllVersionsOfGroupQuery, params, 10000);

        if (!includeInvalid) {
            result = result
                    .stream()
                    .filter(tv -> isValidVersionString(tv.getPackageName(), tv.getVersion()))
                    .collect(Collectors.toList());
        }

        return result;
    }

    public List<TargetVersionModel> getAllScheduledGroupVersions(InstallGroupModel installGroup, String packageName) {
        return getAllScheduledGroupVersions(installGroup.getGroupId(), packageName);
    }

    public List<TargetVersionModel> getAllScheduledGroupVersions(String groupId, String packageName) {
        final Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID_PARAM, groupId);
        params.put(NOW_PARAM, new Date());

        if (StringUtils.isNotBlank(packageName)) {
            params.put(PACKAGE_PARAM, packageName);
        }

        return dbSession.query(getScheduledUpdatesForGroupQuery, params, 10000)
                .stream()
                .filter(tv -> isValidVersionString(tv.getPackageName(), tv.getVersion()))
                .collect(Collectors.toList());
    }

    public TargetVersionModel getNextScheduledGroupVersion(InstallGroupModel installGroupModel, String packageName) {
        return getNextScheduledGroupVersion(installGroupModel.getGroupId(), packageName);
    }

    public TargetVersionModel getNextScheduledGroupVersion(String groupId, String packageName) {
        return getAllScheduledGroupVersions(groupId, packageName)
                .stream()
                .filter(tv -> isValidVersionString(tv.getPackageName(), tv.getVersion()))
                .findFirst()
                .orElse(null);
    }

    public void saveGroup(InstallGroupModel model) {
        dbSession.save(model);
    }

    public void scheduleVersion(String groupId, String packageName, String targetVersion, Date effectiveTime) {
        if (!isValidVersionString(packageName, targetVersion)) {
            throw new IllegalArgumentException("invalid target version");
        }

        if (effectiveTime == null) {
            effectiveTime = new Date();
        }

        dbSession.save(TargetVersionModel.builder()
                .groupId(groupId)
                .packageName(packageName)
                .version(targetVersion)
                .effectiveTime(effectiveTime)
                .build());
    }

    public void removeTargetVersion(int id, String groupId, String packageName) {
        dbSession.delete(TargetVersionModel.builder()
                .id(id)
                .groupId(groupId)
                .packageName(packageName)
                .build());
    }

    public List<InstallGroupMemberModel> getMembersOfGroup(String groupId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID_PARAM, groupId);

        return dbSession.query(getGroupMembersQuery, params, 10000);
    }

    public InstallGroupMemberModel getGroupMember(String groupId, String businessUnitId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(GROUP_ID_PARAM, groupId);
        params.put("businessUnitId", businessUnitId);

        return dbSession.query(findGroupMemberQuery, params, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public void addMemberToGroup(String groupId, String businessUnitId) {
        final InstallGroupMemberModel newMember = InstallGroupMemberModel.builder()
                .groupId(groupId)
                .businessUnitId(businessUnitId)
                .build();

        dbSession.save(newMember);
    }

    public void removeGroup(InstallGroupModel group) {
        final List<TargetVersionModel> allGroupVersions = getAllGroupVersions(group, true);
        if (!allGroupVersions.isEmpty()) {
            dbSession.getBulkOperations().delete(allGroupVersions);
        }

        final List<InstallGroupMemberModel> groupMembers = getMembersOfGroup(group.getGroupId());
        if (!groupMembers.isEmpty()) {
            dbSession.getBulkOperations().delete(groupMembers);
        }

        dbSession.delete(group);
    }

    public void removeGroupMember(InstallGroupMemberModel member) {
        dbSession.delete(member);
    }

    public void removeTargetVersion(TargetVersionModel version) { dbSession.delete(version); }

    private boolean isValidVersionString(String packageName, String versionString) {
        return versionString != null && ("latest".equals(versionString) || tryParseVersion(packageName, versionString).isPresent());
    }

    private Optional<Version> tryParseVersion(String packageName, String versionString) {
        try {
            return Optional.of(versioning.forPackage(packageName).fromString(versionString));
        } catch (Exception ex) {
            log.error("failed to parse version string from database '{}'", versionString, ex);
            return Optional.empty();
        }
    }

    private InstallGroupModel coerceWithTargetVersion(InstallGroupModel group) {
        if (group == null) {
            return null;
        }

        final Map<String, TargetVersionModel> vm = getAllTargetVersions(group.getGroupId());

        if (vm != null) {
            group.setPackageVersions(vm.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().version
                    )));
        }

        return group;
    }
}
