package org.jumpmind.pos.core.flow;

import java.util.Map;

public interface IStateManagerContainer {

    IStateManager create(String appId, String deviceId, Map<String, Object> queryParams, Map<String, String> personalizationProperties);

    IStateManager retrieve(String deviceId, boolean forUseAsDevice);

    void remove(String deviceId);

    void setCurrentStateManager(IStateManager stateManager);

    IStateManager getCurrentStateManager();

    void changeAppId(String deviceId, String appId);

    void changeBrand(String deviceId, String brand);

}