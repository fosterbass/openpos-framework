package org.jumpmind.pos.update.model;

import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.update.UpdateModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(UpdateModule.NAME)
@Component
public class InstallRepository {

    @Autowired
    @Qualifier("updateSession")
    DBSession dbSession;

    public InstallGroupModel findInstallGroup(String installationId) {
        InstallGroupModel installGroupModel = null;
        InstallGroupMemberModel memberModel = dbSession.findByNaturalId(InstallGroupMemberModel.class, installationId);
        if (memberModel != null) {
            installGroupModel = dbSession.findByNaturalId(InstallGroupModel.class, memberModel.getGroupId());
        }
        return installGroupModel;
    }

}
