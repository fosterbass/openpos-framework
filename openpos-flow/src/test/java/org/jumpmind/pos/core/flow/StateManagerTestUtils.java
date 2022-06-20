package org.jumpmind.pos.core.flow;

import org.jumpmind.pos.core.flow.config.FlowConfig;
import org.jumpmind.pos.core.flow.config.TransitionStepConfig;
import org.jumpmind.pos.core.flow.config.YamlConfigProvider;
import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.server.service.IMessageService;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.pos.util.model.Message;
import org.jumpmind.pos.util.startup.DeviceStartupTaskConfig;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;

public class StateManagerTestUtils {

    protected static StateManager buildStateManager(Injector injector, String yamlConfigPath) throws Exception {
        
        YamlConfigProvider provider = new YamlConfigProvider();

        FlowConfig flowConfig = null;

        if (yamlConfigPath != null) {
            provider.load("pos", yamlConfigPath, "TestFlow");
            flowConfig = provider.getConfigByName("pos", "100-1", "TestDoubleSubstateExitFlow");
        }
        
        StateManager stateManager = new StateManager();

        IMessageService messageService = Mockito.mock(IMessageService.class);
        doNothing().when(messageService).sendMessage(any(String.class), any(Message.class));

        DeviceStartupTaskConfig deviceStartupTaskConfig = Mockito.mock(DeviceStartupTaskConfig.class);
        TestUtil.setField(stateManager, "deviceStartupTaskConfig", deviceStartupTaskConfig);

        ActionHandlerImpl actionHandler = new ActionHandlerImpl();
        TestUtil.setField(actionHandler, "beforeActionService" , new BeforeActionStateLifecycleService());
        TestUtil.setField(actionHandler, "helper" , new ActionHandlerHelper());
        TestUtil.setField(stateManager, "actionHandler", actionHandler);
        TestUtil.setField(stateManager, "injector", injector);
        TestUtil.setField(stateManager, "outjector", new Outjector());
        TestUtil.setField(stateManager, "transitionStepConfigs", Arrays.asList(new TransitionStepConfig(TestTransitionStepCancel.class), new TransitionStepConfig(TestTransitionStepProceed.class)));
        TestUtil.setField(stateManager, "stateLifecycle", new StateLifecycle());
        TestUtil.setField(stateManager, "messageService", messageService);
        ClientContext clientContext = new ClientContext();
        TestUtil.setField(stateManager, "clientContext",clientContext);
        StateManagerContainer stateManagerContainer = new StateManagerContainer();
        TestUtil.setField(stateManagerContainer, "clientContext", clientContext);
        TestUtil.setField(stateManager, "stateManagerContainer", stateManagerContainer);

        if (flowConfig != null) {
            stateManager.setInitialFlowConfig(flowConfig);
            stateManager.init(new Device("pos", "100-1"));
            Thread.sleep(500);
        }
        
        return stateManager;
    }

    public static void doAction(StateManager stateManager, Action action) {
        stateManager.doAction(action);
        action.awaitProcessing();

        try {
            while (!stateManager.isAtRest()) {
                Thread.sleep(10);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public static void doAction(StateManager stateManager, String actionName) {
        Action action = new Action(actionName);
        doAction(stateManager, action);
    }


}
