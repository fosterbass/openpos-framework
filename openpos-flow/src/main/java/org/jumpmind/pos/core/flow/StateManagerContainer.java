/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 * <p>
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * http://www.gnu.org/licenses.
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.pos.core.flow;

import org.jumpmind.pos.core.error.IErrorHandler;
import org.jumpmind.pos.core.flow.config.IFlowConfigProvider;
import org.jumpmind.pos.core.flow.config.TransitionStepConfig;
import org.jumpmind.pos.util.Version;
import org.jumpmind.pos.util.Versions;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.pos.util.event.Event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jumpmind.pos.util.AppUtils.setupLogging;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
public class StateManagerContainer implements IStateManagerContainer, ApplicationListener<Event> {
    private final Map<String, StateManager> stateManagersByDeviceId = new HashMap<>();
    private final ThreadLocal<IStateManager> currentStateManager = new InheritableThreadLocal<>();

    @Autowired
    private IFlowConfigProvider flowConfigProvider;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private IErrorHandler errorHandler;

    @Autowired
    private ClientContext clientContext;

    @Autowired(required = false)
    private List<IClientContextUpdater> clientContextUpdaters;

    private Map<String,String> versions;

    @Override
    public synchronized IStateManager retrieve(String deviceId, boolean forUseAsDevice) {
        IStateManager stateManager = stateManagersByDeviceId.get(deviceId);
        if (forUseAsDevice) {
            setCurrentStateManager(stateManager);
        }
        return stateManager;
    }

    @Override
    public synchronized IStateManager create(String appId, String deviceId, Map<String, Object> queryParams, Map<String, String> personalizationProperties) {
        StateManager stateManager = stateManagersByDeviceId.get(deviceId);
        if (stateManager == null) {
            stateManager = applicationContext.getBean(StateManager.class);
            clientContext.put("deviceId", deviceId);
            clientContext.put("appId", appId);
            setCurrentStateManager(stateManager);
            setClientContextVersions();
            if (personalizationProperties != null) {
                personalizationProperties.forEach((key, value) -> clientContext.put(key, value));
            }

            stateManager.setTransitionSteps(createTransitionSteps(appId, deviceId));
            stateManager.registerQueryParams(queryParams);
            stateManager.registerPersonalizationProperties(personalizationProperties);
            stateManager.setErrorHandler(errorHandler);
            stateManager.setInitialFlowConfig(flowConfigProvider.getConfig(appId, deviceId));
            stateManagersByDeviceId.put(deviceId, stateManager);
            stateManager.init(new Device(appId, deviceId));
        }
        return stateManager;
    }

    @Override
    public void changeAppId(String deviceId, String appId) {
        StateManager stateManager = stateManagersByDeviceId.get(deviceId);
        if (stateManager != null) {
            stateManager.getApplicationState().setAppId(appId);
            stateManager.setInitialFlowConfig(flowConfigProvider.getConfig(appId, deviceId));
            stateManager.reset();
        }
    }

    @Override
    public void changeBrand(String deviceId, String brand) {
        IStateManager stateManager = stateManagersByDeviceId.get(deviceId);
        if (stateManager != null) {
            stateManager.getDeviceVariables().put("brandId", brand);
            Map<String, String> properties = new HashMap<>(stateManager.getApplicationState().getScopeValue("personalizationProperties"));
            properties.put("brandId", brand);
            stateManager.registerPersonalizationProperties(properties);
            stateManager.sendConfigurationChangedMessage();
            stateManager.reset();
        }
    }

    @Override
    public void resetStateManager(String deviceId) {
        IStateManager stateManager = stateManagersByDeviceId.get(deviceId);
        if (stateManager != null) {
            stateManager.reset();
        }
    }

    private List<TransitionStepConfig> createTransitionSteps(String appId, String deviceId) {
        List<TransitionStepConfig> transitionStepConfigs = flowConfigProvider.getTransitionStepConfig(appId, deviceId);
        if (CollectionUtils.isEmpty(transitionStepConfigs)) {
            log.info("No configured transition steps found for appId {} deviceId {}. Using discovered steps from Spring.", appId, deviceId);
            transitionStepConfigs = createTransitionStepsFromSpring();
        }
        return transitionStepConfigs;
    }

    private List<TransitionStepConfig> createTransitionStepsFromSpring() {
        List<TransitionStepConfig> steps = new ArrayList<>();
        String[] names = applicationContext.getBeanNamesForType(ITransitionStep.class);
        for (String name : names) {
            TransitionStepConfig config = new TransitionStepConfig();
            config.setTransitionStepClass((Class<? extends ITransitionStep>) applicationContext.getBean(name).getClass());
            steps.add(config);
        }

        steps.sort((o1, o2) -> {
            int o1order = 0;
            int o2order = 0;
            try {
                o1order = o1.getTransitionStepClass().getAnnotation(Order.class).value();
            }
            catch (NullPointerException ex) {
            }
            try {
                o2order = o2.getTransitionStepClass().getAnnotation(Order.class).value();
            }
            catch (NullPointerException ex) {
            }

            return Integer.compare(o1order, o2order);
        });
        return steps;
    }

    @Override
    public synchronized void remove(String deviceId) {
        IStateManager stateManager = stateManagersByDeviceId.remove(deviceId);
        if (stateManager != null) {
            stateManager.stop();
        }
    }

    public synchronized List<StateManager> getAllStateManagers() {
        return new ArrayList<>(stateManagersByDeviceId.values());
    }

    public void setCurrentStateManager(IStateManager stateManager) {
        currentStateManager.set(stateManager);
        if (stateManager != null && stateManager.getDeviceVariables() != null) {
            Device device = stateManager.getDevice();

            if (device != null) {
                setupLogging(stateManager.getDevice().getDeviceId());
            } else {
                setupLogging("unknown");
            }

            for (String property : stateManager.getDeviceVariables().keySet()) {
                clientContext.put(property, stateManager.getDeviceVariables().get(property));
            }

            if (stateManager.getApplicationState() != null && stateManager.getApplicationState().getDeviceMode() != null) {
                clientContext.put("deviceMode", stateManager.getApplicationState().getDeviceMode());
            }

            if (stateManager.getDevice() != null && isNotBlank(stateManager.getDevice().getDeviceId())) {
                clientContext.put("deviceId", stateManager.getDevice().getDeviceId());
            }
            if (stateManager.getDevice() != null && isNotBlank(stateManager.getDevice().getAppId())) {
                clientContext.put("appId", stateManager.getDevice().getAppId());
            }

            setClientContextVersions();
            if (clientContextUpdaters != null) {
                for (IClientContextUpdater clientContextUpdater : clientContextUpdaters) {
                    clientContextUpdater.update(clientContext, stateManager);
                }
            }

        } else {
            setupLogging("server");
        }
    }

    public IStateManager getCurrentStateManager() {
        return currentStateManager.get();
    }

    @Override
    public void onApplicationEvent(Event event) {
        for (StateManager stateManager : new ArrayList<>(stateManagersByDeviceId.values())) {
            stateManager.onEvent(event);
        }
    }

    private void setClientContextVersions() {
        getVersions().forEach((key, value) -> clientContext.put("version." + key, value));
    }

    private Map<String,String> getVersions() {
        if (null == versions) {
            versions = Versions.getVersions().stream().collect(Collectors.toMap(Version::getComponentName, Version::getVersion));
        }

        return versions;
    }
}
