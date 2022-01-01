package org.jumpmind.pos.update.model;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.persist.Query;
import org.jumpmind.pos.update.UpdateModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Profile(UpdateModule.NAME)
@Component
public class InstallRepository {


    @Autowired
    @Qualifier("updateSession")
    DBSession dbSession;

    private final Query<InstallGroupModel> getGroupsPairedWithAllActiveVersionsQuery = new Query<InstallGroupModel>()
            .named("getGroupsPairedWithAllActiveVersions")
            .result(InstallGroupModel.class);

    private final Query<InstallGroupModel> findGroupForInstallation = new Query<InstallGroupModel>()
            .named("findGroupForInstallation")
            .result(InstallGroupModel.class);

    private final Query<TargetVersionModel> getGroupVersionHistory = new Query<TargetVersionModel>()
            .named("getActiveVersionHistoryOfGroup")
            .result(TargetVersionModel.class);

    private final Query<TargetVersionModel> getScheduledUpdatesForGroupQuery = new Query<TargetVersionModel>()
            .named("getScheduledVersionsOfGroup")
            .result(TargetVersionModel.class);

    public InstallGroupModel findInstallGroup(String installationId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("installationId", installationId);

        return dbSession.query(findGroupForInstallation, params, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<InstallGroupModel> getInstallGroups() {
        return getInstallGroups(null);
    }

    public List<InstallGroupModel> getInstallGroups(String filter) {
        final Map<String, Object> params = new HashMap<>();
        params.put("now", new Date());

        if (StringUtils.isNotEmpty(filter)) {
            params.put("filter", "%" + filter + "%");
        }

        final List<InstallGroupModel> queryResults = dbSession.query(
                getGroupsPairedWithAllActiveVersionsQuery,
                params,
                10000);

        return new ArrayList<>(queryResults.stream()

                // Due to some ORM limitations the incoming query
                // results will contain the same group paired with
                // every version that has ever been effective for
                // that group. We'll process it in memory to only pick a
                // one of the results.
                //
                // The query results ordering is important. It should
                // always come in with the latest activated first.
                .collect(Collectors.toMap(
                        InstallGroupModel::getGroupId,
                        Function.identity(),
                        (existing, ignored) -> existing
                ))
                .values());
    }

    public List<TargetVersionModel> getAllScheduledGroupVersions(InstallGroupModel installGroup) {
        return getAllScheduledGroupVersions(installGroup.getGroupId());
    }

    public List<TargetVersionModel> getAllScheduledGroupVersions(String groupId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("now", new Date());

        return dbSession.query(getScheduledUpdatesForGroupQuery, params, 10000);
    }

    public TargetVersionModel getNextScheduledGroupVersion(InstallGroupModel installGroupModel) {
        return getNextScheduledGroupVersion(installGroupModel.getGroupId());
    }

    public TargetVersionModel getNextScheduledGroupVersion(String groupId) {
        return getAllScheduledGroupVersions(groupId)
                .stream()
                .findFirst()
                .orElse(null);
    }
}
