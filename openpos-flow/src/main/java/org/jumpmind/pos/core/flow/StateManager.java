/**
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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jumpmind.pos.core.clientconfiguration.ClientConfigChangedMessage;
import org.jumpmind.pos.core.clientconfiguration.IClientConfigSelector;
import org.jumpmind.pos.core.error.IErrorHandler;
import org.jumpmind.pos.core.event.DeviceResetEvent;
import org.jumpmind.pos.core.flow.config.*;
import org.jumpmind.pos.core.model.DataClearMessage;
import org.jumpmind.pos.core.model.StartupMessage;
import org.jumpmind.pos.core.service.IScreenService;
import org.jumpmind.pos.core.service.UIDataMessageProviderService;
import org.jumpmind.pos.core.service.spring.DeviceScope;
import org.jumpmind.pos.core.ui.CloseToast;
import org.jumpmind.pos.core.ui.DialogProperties;
import org.jumpmind.pos.core.ui.Toast;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.data.UIDataMessageProvider;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.server.service.IMessageService;
import org.jumpmind.pos.util.ClassUtils;
import org.jumpmind.pos.util.Versions;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.pos.util.event.AppEvent;
import org.jumpmind.pos.util.event.Event;
import org.jumpmind.pos.util.event.EventPublisher;
import org.jumpmind.pos.util.model.PrintMessage;
import org.jumpmind.pos.util.startup.DeviceStartupTaskConfig;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.lang.String.*;

@Slf4j
@Component()
@org.springframework.context.annotation.Scope("prototype")
public class StateManager implements IStateManager {

    final static AtomicInteger threadCounter = new AtomicInteger(1);

    @Autowired
    IScreenService screenService;

    @Autowired
    ActionHandlerImpl actionHandler;

    @Autowired
    Injector injector;

    @Autowired
    Outjector outjector;

    @Autowired(required = false)
    List<? extends ISessionListener> sessionListeners;

    @Autowired
    @Getter
    StateManagerObservers stateManagerObservers;

    @Autowired
    IMessageService messageService;

    @Autowired
    UIDataMessageProviderService uiDataMessageProviderService;

    @Autowired
    IClientConfigSelector clientConfigSelector;

    @Autowired
    StateLifecycle stateLifecycle;

    @Autowired
    ActionHandlerHelper helper;

    @Autowired
    DeviceStartupTaskConfig deviceStartupTaskConfig;

    @Autowired
    StateManagerContainer stateManagerContainer;

    @Autowired
    Environment env;

    @Autowired
    ScreensConfig screensConfig;

    @Value("${openpos.general.failOnUnmatchedAction:false}")
    boolean failOnUnmatchedAction;

    @Autowired
    ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor;

    @Autowired
    EventPublisher eventPublisher;

    @Autowired
    ClientContext clientContext;

    ApplicationState applicationState = new ApplicationState();

    List<TransitionStepConfig> transitionStepConfigs;

    FlowConfig initialFlowConfig;

    long sessionTimeoutMillis = 0;

    Action sessionTimeoutAction;

    Map<String, String> deviceVariables = new HashMap<>();

    IErrorHandler errorHandler;

    EventBroadcaster eventBroadcaster;

    final String STATE_MANAGER_RESET_ACTION = "StateManagerReset";
    final String STATE_MANAGER_STOP_ACTION = "StateManagerStop";
    final String STATE_MANAGER_PROCESS_EVENT_ACTION = "StateManagerProcessEvent";

    AtomicBoolean runningFlag = new AtomicBoolean(false);
    AtomicBoolean busyFlag = new AtomicBoolean(false);

    AtomicReference<Date> lastInteractionTime = new AtomicReference<Date>(new Date());
    AtomicBoolean transitionRestFlag = new AtomicBoolean(false);
    AtomicBoolean connected = new AtomicBoolean(false);
    AtomicLong lastShowTimeInMs = new AtomicLong(0);

    @Override
    public void reset() {
        reset(null);
    }

    @Override
    public void reset(Scope initialScope) {
        log.info("StateManager reset queued");
        this.actionQueue.clear();

        if (initialScope == null) {
            initialScope = new Scope();
        }

        initialScope.setDeviceScope("parentDevice", applicationState.getScopeValue(ScopeType.Device, "parentDevice"));
        initialScope.setDeviceScope("childDevices", applicationState.getScopeValue(ScopeType.Device, "childDevices"));

        final Action resetAction = Action.builder()
                .name(STATE_MANAGER_RESET_ACTION)
                .data(initialScope)
                .build();

        boolean offered = this.actionQueue.offer(new ActionContext(resetAction));
        if (!offered) {
            log.warn("StateManager failed to insert reset action into the queue");
        }
    }

    @Override
    public void stop() {
        log.info("StateManager stopping.");
        this.actionQueue.clear();
        boolean offered = this.actionQueue.offer(new ActionContext(new Action(STATE_MANAGER_STOP_ACTION)));
        if (!offered) {
            log.warn("StateManager failed to insert stop action into the queue");
        }
    }

    @Override
    public int getActionQueueSize() {
        return actionQueue.size();
    }

    @Override
    public void init(Device device) {
        init(device, null);
    }

    public void init(Device device, Scope initialScope) {
        this.applicationState.reset(scheduledAnnotationBeanPostProcessor, initialScope);
        this.applicationState.setAppId(device.getAppId());
        this.applicationState.setDeviceId(device.getDeviceId());
        this.eventBroadcaster = new EventBroadcaster(this);

        applicationState.getScope().setDeviceScope("stateManager", this);

        initDefaultScopeObjects();

        if (initialFlowConfig != null) {
            applicationState.setCurrentContext(new StateContext(initialFlowConfig, null, null));
            sendConfigurationChangedMessage();

            deviceStartupTaskConfig.processDeviceStartupTasks(device.getDeviceId(), device.getAppId());

            sendStartupCompleteMessage();

            if (initialFlowConfig.getInitialState() == null ||
                    initialFlowConfig.getInitialState().getStateClass() == null) {
               throw new IllegalStateException(format("The flow for %s did not have an initial state configured", getDevice()));
            }

            startActionLoop(StateManagerActionConstants.STARTUP_ACTION, initialFlowConfig.getInitialState());
        } else {
            throw new IllegalStateException("Could not find a flow config for " + device.getAppId());
        }

    }

    protected void startActionLoop(final String startupAction, final StateConfig initialState) {
        String threadName = "StateManagerThread" + threadCounter.incrementAndGet() + "(" + applicationState.getAppId() + ":" + applicationState.getDeviceId() + ")";
        Thread stateManagerThread = new Thread(threadName) {
            public void run() {
                try {
                    stateManagerContainer.setCurrentStateManager(StateManager.this);
                    initClientContext();  // Init ClientContext threadLocal properties for this thread
                    transitionTo(new Action(startupAction), initialState);
                    runningFlag.set(true);
                    actionLoop();
                } catch (Throwable ex) {
                    log.error("Unhandled exception from StateManager thread. StateManagerThread exiting.", ex);
                }

            }
        };

        stateManagerThread.setDaemon(true);
        log.info("Starting stateManager thread: " + threadName);
        stateManagerThread.start();
    }

    BlockingQueue<ActionContext> actionQueue = new LinkedBlockingQueue<ActionContext>();

    protected void actionLoop() {
        while (runningFlag.get()) {
            ActionContext actionContext = null;
            try {
                actionContext = actionQueue.poll(60, TimeUnit.SECONDS);
                if (actionContext != null) {
                    // an action may originally come from a device but then get forwarded back through here from a state.
                    // we only want to know that it originated from the screen/device the first time it came through.
                    actionContext.getAction().setOriginatesFromDeviceFlag(false);
                    busyFlag.set(true);
                    if (actionContext.getAction().getName().equals(STATE_MANAGER_RESET_ACTION)) {
                        final Scope initialScope = actionContext.getAction().getData();

                        log.info("StateManager reset queued");
                        actionContext.getAction().markProcessed();
                        runningFlag.set(false);
                        busyFlag.set(false);
                        init(getDevice(), initialScope);
                        log.info("StateManager reset");
                        this.eventPublisher.publish(new DeviceResetEvent(getDevice().getDeviceId(), getDevice().getAppId()));
                        break;
                    } else if (actionContext.getAction().getName().equals(STATE_MANAGER_STOP_ACTION)) {
                        actionContext.getAction().markProcessed();
                        runningFlag.set(false);
                        busyFlag.set(false);
                        log.info("StateManager stopped");
                        break;
                    } else if (actionContext.getAction().getName().equals(STATE_MANAGER_PROCESS_EVENT_ACTION)) {
                        processEvent(actionContext.getAction().getData());
                        actionContext.getAction().markProcessed();
                    } else {
                        log.info("###### last known queue size: " + actionContext.getAction().getLastKnownQueueSize());
                        clientContext.put(ClientContext.CLIENT_QUEUE_SIZE, Integer.toString(actionContext.getAction().getLastKnownQueueSize()));
                        processAction(actionContext);
                        actionContext.getAction().markProcessed();
                    }
                    if (actionQueue.size() == 0) {
                        busyFlag.set(false);
                    }
                }
            } catch (InterruptedException ex) {
                log.warn("StateManager thread was interrupted, exiting.", ex);
                busyFlag.set(false);
                runningFlag.set(false);
            } catch (Throwable ex) {
                busyFlag.set(false);
                handleOrRaiseException(ex);
            } finally {
                if (actionContext != null) {
                    actionContext.getAction().markProcessed();
                }
            }
        }
        log.info("State action actionLoop is exiting.");
    }

    public void sendDataClearMessage() {
        String deviceId = applicationState.getDeviceId();
        messageService.sendMessage(deviceId, new DataClearMessage());
    }

    public void sendStartupCompleteMessage() {
        String deviceId = applicationState.getDeviceId();
        messageService.sendMessage(deviceId, new StartupMessage(true, "StateManager Startup Complete"));
    }

    public void sendPrintMessage(PrintMessage message) {
        String deviceId = applicationState.getDeviceId();
        messageService.sendMessage(deviceId, message);
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    @Override
    public boolean isConnected() {
        return this.connected.get();
    }

    protected void setTransitionSteps(List<TransitionStepConfig> transitionStepConfigs) {
        this.transitionStepConfigs = transitionStepConfigs;
    }

    void initDefaultScopeObjects() {
        applicationState.getScope().setDeviceScope("additionalTagsForConfiguration", new ArrayList<String>());
    }

    public void setErrorHandler(IErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public IErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void setDeviceVariables(Map<String, String> deviceVariables) {
        this.deviceVariables = deviceVariables;
    }

    @Override
    public Map<String, String> getDeviceVariables() {
        return this.deviceVariables;
    }

    protected void transitionTo(Action action, StateConfig stateConfig) {
        Object newState = buildState(stateConfig);
        transitionTo(action, newState);
    }

    @Override
    public void transitionTo(Action action, Object newState) {
        transitionTo(action, newState, null, null);
    }

    protected void transitionTo(Action action, Object newState, SubFlowConfig enterSubStateConfig, StateContext resumeSuspendedState) {
        transitionTo(action, newState, enterSubStateConfig, resumeSuspendedState, false);
    }

    protected void transitionTo(Action action, Object newState, SubFlowConfig enterSubStateConfig, StateContext resumeSuspendedState,
                                boolean autoTransition) {
        if (applicationState.getCurrentContext() == null) {
            throw new FlowException(
                    "There is no applicationState.getCurrentContext() on this StateManager.  HINT: States should use @In to get the StateManager, not @Autowired.");
        }

        if (enterSubStateConfig != null && resumeSuspendedState != null) {
            throw new FlowException(
                    "enterSubStateConfig and resumeSuspendedState should not BOTH be provided at the same time. enterSubStateConfig implies entering a subState, "
                            + "while resumeSuspendedState implies "
                            + "exiting a subState. These two should not be happening at the same time.");
        }

        boolean enterSubState = enterSubStateConfig != null;
        stateLifecycle.executeDepart(applicationState.getCurrentContext().getState(), newState, enterSubState, action);

        if (applicationState.getCurrentContext().getState() != null) {
            performOutjections(applicationState.getCurrentContext().getState());
        }

        Transition transition = new Transition(transitionStepConfigs,
                applicationState.getCurrentContext(), newState, enterSubStateConfig, resumeSuspendedState, autoTransition);
        transition.setStateManager(this);
        transition.setOriginalAction(action);

        beginTransition(transition);
    }

    protected String getReturnActionName(Action action) {
        if (applicationState.getCurrentContext().getSubFlowConfig() != null
                && applicationState.getCurrentContext().getSubFlowConfig().getReturnActionNames() != null) {

            for (String returnActionName : applicationState.getCurrentContext().getSubFlowConfig().getReturnActionNames()) {
                if (StringUtils.equals(returnActionName, action.getName())) {
                    return action.getName(); // the action IS a return action,
                    // so don't modify.
                }
            }

            if (applicationState.getCurrentContext().getSubFlowConfig().getReturnActionNames().length == 1) {
                return applicationState.getCurrentContext().getSubFlowConfig().getReturnActionNames()[0];
            } else if (applicationState.getCurrentContext().getSubFlowConfig().getReturnActionNames().length > 1) {
                throw new FlowException("Unexpected situation. Non-return action raised which completed a subflow: " + action.getName()
                        + " -- but there is more than 1 return action mapped to this subflow so we don't know which return action to pick: "
                        + Arrays.toString(applicationState.getCurrentContext().getSubFlowConfig().getReturnActionNames()));
            }
        }

        return null;
    }

    protected StateContext buildSubStateContext(SubFlowConfig enterSubStateConfig, Action action) {
        StateContext stateContext = new StateContext(enterSubStateConfig.getSubFlowConfig(), action);
        stateContext.getFlowScope().putAll(applicationState.getCurrentContext().getFlowScope());
        stateContext.setSubFlowConfig(enterSubStateConfig);
        return stateContext;
    }

    public void performInjectionsOnSpringBean(Object springBean) {
        injector.performInjectionsOnSpringBean(springBean, applicationState.getScope(), applicationState.getCurrentContext());
    }

    public void performInjections(Object stateOrStep) {
        this.log.trace("Performing injections on {}...", stateOrStep.getClass().getName());
        injector.performInjections(stateOrStep, applicationState.getScope(), applicationState.getCurrentContext());
        refreshDeviceScope();
        this.log.trace("Injections completed on {}.", stateOrStep.getClass().getName());
    }

    protected void refreshDeviceScope() {
        for (String name : new HashSet<>(applicationState.getScope().getDeviceScope().keySet())) {
            if (!shouldWireDeviceBeanNamed(name)) {
                continue;
            }
            Object value = applicationState.getScopeValue(ScopeType.Device, name);
            if (value == null || !shouldWireDeviceBean(value)) {
                continue;
            }

            performOutjections(value);
            if (DeviceScope.isDeviceScope(name)) {
                performInjectionsOnSpringBean(value);
            } else {
                injector.performInjections(value, applicationState.getScope(), applicationState.getCurrentContext());
            }
        }
    }

    protected boolean shouldWireDeviceBean(Object value) {
        Class<?> clazz = value.getClass();
        if (ClassUtils.isSimpleType(clazz)
            || Collection.class.isAssignableFrom(clazz)
            || Map.class.isAssignableFrom(clazz)) {
            return false;
        } else {
            return true;
        }
    }

    protected boolean shouldWireDeviceBeanNamed(String name) {
        switch (name) {
            case "stateManager":
            case "queryParams":
            case "device":
            case "businessUnit":
            case "deviceId":
            case "personalizationProperties":
                return false;
            default:
                return true;
        }
    }

    public void pushScopeValue(String name, ScopeType scopeType, Object value) {
        applicationState.getScope().setScope(name, scopeType, value);
        refreshDeviceScope();
    }

    public void performOutjections(Object stateOrStep) {
        outjector.performOutjections(stateOrStep, applicationState.getScope(), applicationState.getCurrentContext());
    }

    protected void beginTransition(Transition transition) {
        applicationState.setCurrentTransition(transition);

        transition.begin();
        checkTransitionEnd(transition.getOriginalAction());
    }

    void continueTransition(Action action) {
        Transition transition = applicationState.getCurrentTransition();
        boolean handled = applicationState.getCurrentTransition().handleAction(action);
        if (!handled) {
            throw new FlowException("Transition step " + applicationState.getCurrentTransition().getCurrentTransitionStep() + " cannot handle action '" + action + "'");
        }

        checkTransitionEnd(transition.getOriginalAction());
    }

    void checkTransitionEnd(Action action) {
        if (applicationState.getCurrentTransition().getTransitionResult()
                != TransitionResult.TransitionResultCode.IN_PROGRESS) {
            TransitionResult transitionResult = new TransitionResult();
            transitionResult.setTransitionResultCode(applicationState.getCurrentTransition().getTransitionResult());
            transitionResult.setTransition(applicationState.getCurrentTransition());
            applicationState.setCurrentTransition(null);

            if (transitionResult.getTransitionResultCode() == TransitionResult.TransitionResultCode.PROCEED) {
                completeTransition(transitionResult, action);
            } else {
                cancelTransition(transitionResult, action);
            }
        }
    }

    void completeTransition(TransitionResult transitionResult, Action action) {
        Transition transition = transitionResult.getTransition();
        String returnActionName = null;
        if (transition.isExitingSubstate()) {
            returnActionName = getReturnActionName(action);
        }

        if (stateManagerObservers != null) {
            stateManagerObservers.onTransition(applicationState, transition, action, returnActionName);
        }

        if (transition.isEnteringSubstate()) {
            applicationState.getStateStack().push(applicationState.getCurrentContext());
            applicationState.setCurrentContext(buildSubStateContext(transition.getEnterSubStateConfig(), action));
        } else if (transition.isExitingSubstate()) {
            applicationState.setCurrentContext(transition.getResumeSuspendedState());
        }

        applicationState.getCurrentContext().setState(transition.getTargetState());

        if (transitionResult != null && transitionResult.getTransition() != null) {
            for (ITransitionStep transitionStep : transitionResult.getTransition().getTransitionSteps()) {
                performInjections(transitionStep);
                transitionStep.afterTransition(new TransitionContext(action, applicationState.getCurrentContext()));
                performOutjections(transitionStep);
            }
        }

        performInjections(transition.getTargetState());

        if (transition.getResumeSuspendedState() == null || returnActionName == null || transition.isAutoTransition()) {
            stateLifecycle.executeArrive(this, applicationState.getCurrentContext().getState(), action);
        } else {
            Action returnAction = new Action(returnActionName, action.getData());
            returnAction.setCausedBy(action);
            doAction(returnAction); // indirect recursion
        }
    }

    void cancelTransition(TransitionResult transitionResult, Action action) {
        //TODO: discuss whether this is how we want to handle cancelled transitions
        Action cancelAction = new Action(StateManagerActionConstants.TRANSITION_CANCELLED_ACTION);
        cancelAction.setCausedBy(action);
        if (applicationState.getCurrentContext().getState() == null) {
            throw new FlowException("A transition was cancelled but there is no state to go back to. This could be a case where the first thing shown is a transition to the initial state, " +
                    "like a user name prompt leading into self-checkout for example. To correct this, adjust your flow config so there is truly an initial state to go back to when the transition is cancelled.");
        }
        // resassert the previous state before the cancelled transition
        // Note: this can be a problem if the state doesn't show a screen and just sends an action because
        // the StateManager will be busy.
        if (transitionResult.getTransition().getQueuedAction() != null) { // allowed cancelled transitions to queue an action.
            doAction(transitionResult.getTransition().getQueuedAction());
        } else {
            stateLifecycle.executeArrive(this, applicationState.getCurrentContext().getState(), cancelAction);
        }
    }

    protected Object buildState(StateConfig stateConfig) {
        return createNewState(stateConfig.getStateClass());
    }

    @Override
    public Object getCurrentState() {
        if (applicationState.getCurrentContext() != null) {
            return applicationState.getCurrentContext().getState();
        } else {
            throw new FlowException("applicationState.getCurrentContext() is null. This StateManager is likely misconfigured. " +
                    "Check your appId and Spring profiles. (appId=\"" + getDevice().getAppId() +
                    "\") profiles=" + Arrays.toString(env.getActiveProfiles()));
        }
    }

    @Override
    public void refreshScreen() {
        Map<String, UIDataMessageProvider<?>> dataProviders = applicationState.getDataMessageProviderMap();
        uiDataMessageProviderService.resetProviders(applicationState);

        /*
         * Hang onto the dialog since showing the last screen first will clear
         * the last dialog from the screen service
         */
        UIMessage lastDialog = screenService.getLastPreInterceptedDialog(applicationState.getDeviceId());
        UIMessage lastScreen = screenService.getLastPreInterceptedScreen(applicationState.getDeviceId());
        if (lastScreen != null) {
            lastScreen.put("refreshAlways", true);
            showScreen(lastScreen, dataProviders);
        }
        if (lastDialog != null) {
            // Don't save a dialog if it is closable, otherwise it gets shown on
            // a refresh
            DialogProperties properties = (DialogProperties) lastDialog.get("dialogProperties");
            if (properties == null || !properties.isCloseable()) {
                lastDialog.put("refreshAlways", true);
                showScreen(lastDialog, dataProviders);
            }
        }

        Object currentState = getCurrentState();
        if (currentState != null) {
            Class<?> clazz = currentState.getClass();

            List<Method> methods = MethodUtils.getMethodsListWithAnnotation(clazz, OnRefresh.class, true, true);

            if (!methods.isEmpty()) {
                methods.forEach(m -> {
                    m.setAccessible(true);
                    try {
                        m.invoke(getCurrentState());
                    } catch (Exception ex) {
                        if (errorHandler != null) {
                            errorHandler.handleError(this, ex);
                        } else {
                            throw new FlowException("Failed to invoke method " + m, ex);
                        }
                    }
                });
            }
        }

    }

    // Could come from a UI or a running state..
    @Override
    public void doAction(String actionName) {
        doAction(actionName, null);
    }

    @Override
    public void doAction(String actionName, Map<String, String> params) {
        Action action = new Action(actionName, params);
        doAction(action);
    }

    @Override
    public void keepAlive() {
        lastInteractionTime.set(new Date());
    }

    @Override
    public boolean isAtRest() {
        return !busyFlag.get();
    }

    @Override
    public void doAction(Action action) {
        boolean valid = validateAction(action);
        if (!valid) {
            log.warn("Discarding invalid action: " + action);
            return;
        }

        if (stateManagerObservers != null) {
            stateManagerObservers.onAction(applicationState, action);
        }

        ActionContext actionContext = null;
        if (isOnStateManagerThread()) {
            actionContext = new ActionContext(action, Thread.currentThread().getStackTrace());
        } else {
            actionContext = new ActionContext(action);
        }

        actionContext.parseSyncId();

        boolean offered = actionQueue.offer(actionContext);
        if (!offered) {
            log.warn("StateManager failed to insert action {} into the queue", action);
        }
    }

    private boolean validateAction(Action action) {
        if (action == null) {
            log.warn("action passed to state manager was null.");
            return false;
        } else if (action.getName() == null) {
            try {
                log.warn("An action was passed with a null name. An action must have a name. Data: {}, current state: {}", action.getData(),
                        getCurrentState() != null ? getCurrentState().getClass().getSimpleName() : "unknown");
            } catch (FlowException flowException) {
                log.warn(String.format("An action was passed with a null name. An action must have a name. Data: %s, current state: undefined", action.getData()));
            }
            return false;
        }

        return true;
    }


    boolean isOnStateManagerThread() {
        return Thread.currentThread().getName().startsWith("StateManager");
    }

    protected void processAction(ActionContext actionContext) {
        lastInteractionTime.set(new Date());

        boolean syncOk = actionHandler.checkActionSync(this, actionContext);
        if (!syncOk) {
            return;
        }

        Action action = actionContext.getAction();

        try {
            // Global action handler takes precedence over all actions (for now)
            Class<?> globalActionHandler = getGlobalActionHandler(action);
            if (globalActionHandler != null && !isCalledFromGlobalActionHandler(globalActionHandler, actionContext)) {
                callGlobalActionHandler(action, globalActionHandler);
                refreshDeviceScope();
                return;
            }

            if (applicationState.getCurrentTransition() != null) {
                continueTransition(actionContext.getAction());
                return;
            }

            FlowConfig flowConfig = applicationState.getCurrentContext().getFlowConfig();
            StateConfig stateConfig = applicationState.findStateConfig(flowConfig);

            validateStateConfig(applicationState.getCurrentContext().getState(), stateConfig);

            Class<?> transitionStateClass = stateConfig.getActionToStateMapping().get(action.getName());
            Class<?> globalTransitionStateClass = flowConfig.getActionToStateMapping().get(action.getName());
            SubFlowConfig subStateConfig = stateConfig.getActionToSubStateMapping().get(action.getName());
            SubFlowConfig globalSubStateConfig = flowConfig.getActionToSubStateMapping().get(action.getName());

            // Execute state specific action handlers
            if (actionHandler.canHandleAction(applicationState.getCurrentContext().getState(), actionContext)) {
                handleAction(action);
            } else if (handleTerminatingState(action, stateConfig)) {
                return;
            } else if (transitionStateClass != null) {
                // Execute state transition
                transitionToState(action, transitionStateClass);
            } else if (subStateConfig != null) {
                // Execute sub-state transition
                transitionToSubState(action, subStateConfig);
            } else if (actionHandler.canHandleAnyAction(applicationState.getCurrentContext().getState(), actionContext)) {
                // Execute action handler for onAnyAction
                actionHandler.handleAnyAction(this, applicationState.getCurrentContext().getState(), action);
            } else if (globalTransitionStateClass != null) {
                // Execute global state transition
                handleGlobalStateTransition(action, globalTransitionStateClass);
            } else if (globalSubStateConfig != null) {
                // Execute global sub-state transition
                transitionToSubState(action, globalSubStateConfig);
            } else {
                String msg = String.format(
                        "Unexpected action \"%s\". Either no @ActionHandler %s.on%s() method found, or no withTransition(\"%s\"...) defined in the \"%s\" flow config.",
                        action.getName(), applicationState.getCurrentContext().getState().getClass().getName(), action.getName(),
                        action.getName(), applicationState.getCurrentContext().getFlowConfig().getName());
                if (failOnUnmatchedAction) {
                    throw new FlowException(msg);
                } else {
                    log.warn(msg);
                }
            }
        } finally {
            if (action.isDoNotBlockForResponse()) {
                lastShowTimeInMs.set(System.currentTimeMillis());
            }
        }
    }

    protected void processEvent(Event event) {
        if (event instanceof AppEvent &&
                getDevice().getDeviceId().equals(((AppEvent) event).getDeviceId())) {
            lastInteractionTime.set(new Date());
        }
        if (initialFlowConfig == null) {
            throw new FlowException("initialFlowConfig is null. This StateManager is likely misconfigured. " +
                    "Check your appId and Spring profiles. (appId=\"" + getDevice().getAppId() +
                    "\") profiles=" + Arrays.toString(env.getActiveProfiles()));
        }

        List<Class<?>> classes = initialFlowConfig.getEventHandlers();
        classes.forEach(clazz -> eventBroadcaster.postEventToObject(clazz, event));

        applicationState.getScope().getDeviceScope().values().
                forEach(obj->eventBroadcaster.postEventToObject(obj, event));

        Object state = getCurrentState();
        if (state != null) {
            eventBroadcaster.postEventToObject(state, event);
        }

    }

    protected void handleOrRaiseException(Throwable ex) {
        if (this.getErrorHandler() != null) {
            this.getErrorHandler().handleError(this, ex);
        } else {
            throw ex instanceof RuntimeException ? (RuntimeException) ex : new FlowException(ex);
        }
    }

    protected Class<?> getGlobalActionHandler(Action action) {
        FlowConfig flowConfig = applicationState.getCurrentContext().getFlowConfig();
        Class<?> currentActionHandler = flowConfig.getActionToStateMapping().get(action.getName());
        LinkedList<StateContext> stateStack = applicationState.getStateStack();

        if (isActionHandler(currentActionHandler)) {
            return currentActionHandler;
        } else {
            for (StateContext state : stateStack) {
                Class<?> actionHandler = state.getFlowConfig().getActionToStateMapping().get(action.getName());
                if (isActionHandler(actionHandler)) {
                    return actionHandler;
                }
            }
        }

        return null;
    }

    protected boolean isState(Class<?> stateOrActionHandler) {
        if (stateOrActionHandler != null) {
            List<Method> arriveMethods = MethodUtils.getMethodsListWithAnnotation(stateOrActionHandler, OnArrive.class, true, true);
            return CollectionUtils.isNotEmpty(arriveMethods);
        }
        return false;
    }

    protected boolean isActionHandler(Class<?> stateOrActionHandler) {
        if (stateOrActionHandler != null) {
            List<Method> globalMethods = MethodUtils.getMethodsListWithAnnotation(stateOrActionHandler, OnGlobalAction.class);
            return CollectionUtils.isNotEmpty(globalMethods);
        }
        return false;
    }

    void callGlobalActionHandler(Action action, Class<?> globalActionHandler) {
        log.debug("Calling global action handler: {}", globalActionHandler.getName());

        if (isState(globalActionHandler) && isActionHandler(globalActionHandler)) {
            throw new FlowException("Class cannot implement @OnArrive and @OnGlobalAction: " + globalActionHandler.getName());
        }

        Object actionHandler;
        try {
            actionHandler = globalActionHandler.newInstance();
        } catch (Exception ex) {
            throw new FlowException("Failed to execute global action handler: " + globalActionHandler, ex);
        }
        performInjections(actionHandler);
        List<Method> globalMethods = MethodUtils.getMethodsListWithAnnotation(globalActionHandler, OnGlobalAction.class)
                .stream()
                .filter(method -> method.getName().equals("on" + action.getName()))
                .collect(Collectors.toList());
        for (Method method : globalMethods) {
            invokeGlobalAction(action, method, actionHandler);
        }
    }

    protected boolean isCalledFromGlobalActionHandler(Object handler, ActionContext actionContext) {
        StackTraceElement[] stackTrace = actionContext.getStackTrace();

        if (stackTrace == null) {
            return false;
        }

        if (stackTrace.length > 150) {
            helper.checkStackOverflow(StateManager.class, handler, stackTrace);
        }

        for (StackTraceElement stackFrame : stackTrace) {
            Class<?> currentClass = helper.getClassFrom(stackFrame);
            if (currentClass != null && !Modifier.isAbstract(currentClass.getModifiers()) && FlowUtil.isGlobalActionHandler(currentClass)
                    && !currentClass.isAssignableFrom(handler.getClass())) {
                return false;
            } else if (stackFrame.getClassName().equals(((Class) handler).getName())) {
                return true;
            }
        }

        return false;
    }

    protected void invokeGlobalAction(Action action, Method method, Object actionHandler) {
        try {
            if (method.getParameters() != null && method.getParameters().length == 1) {
                method.invoke(actionHandler, action);
            } else {
                method.invoke(actionHandler);
            }
        } catch (Exception ex) {
            throw new FlowException("Failed to execute global action handler. Method: " + method + " actionHandler: " + actionHandler, ex);
        }
    }

    protected void handleGlobalStateTransition(Action action, Class<?> stateOrActionHandler) {
        if (isState(stateOrActionHandler) && isActionHandler(stateOrActionHandler)) {
            throw new FlowException("Class cannot implement @OnArrive and @OnGlobalAction: " + stateOrActionHandler.getName());
        } else if (isState(stateOrActionHandler)) {
            transitionToState(action, stateOrActionHandler);
        }
    }

    public void setScopeValue(ScopeType scopeType, String name, Object value) {
        applicationState.getScope().setScopeValue(scopeType, name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getScopeValue(ScopeType scopeType, String name) {
        return (T) applicationState.getScopeValue(scopeType, name);
    }

    public <T> T getScopeValue(String name) {
        return applicationState.getScopeValue(name);
    }

    protected boolean handleAction(Object state, Action action) {
        return actionHandler.handleAction(this, state, action);
    }

    protected boolean handleAction(Action action) {
        return handleAction(applicationState.getCurrentContext().getState(), action);
    }

    protected boolean handleTerminatingState(Action action, StateConfig stateConfig) {
        if (stateConfig == null || stateConfig.getActionToStateMapping() == null) {
            return false;
        }

        Class<?> targetStateClass = stateConfig.getActionToStateMapping().get(action.getName());

        if (targetStateClass == null) {
            targetStateClass = applicationState.getCurrentContext().getFlowConfig().getActionToStateMapping().get(action.getName());
        }

        if (CompleteState.class == targetStateClass) {
            if (!applicationState.getStateStack().isEmpty()) {
                StateContext suspendedState = applicationState.getStateStack().pop();
                StateConfig suspendedStateConfig = suspendedState.getFlowConfig().getStateConfig(suspendedState.getState());

                String returnAction;
                if (applicationState.getCurrentContext().isSubstateReturnAction(action.getName())) {
                    returnAction = action.getName();
                } else {
                    returnAction = applicationState.getCurrentContext().getPrimarySubstateReturnAction();
                }

                Class<?> autoTransitionStateClass = suspendedStateConfig.getActionToStateMapping().get(returnAction);
                if (autoTransitionStateClass != null && autoTransitionStateClass != CompleteState.class) {
                    transitionTo(action, createNewState(autoTransitionStateClass), null, suspendedState, true);
                } else {
                    SubFlowConfig autoSubFlowConfig = suspendedStateConfig.getActionToSubStateMapping().get(returnAction);
                    if (autoSubFlowConfig != null) {
                        applicationState.setCurrentContext(suspendedState);
                        transitionToSubState(action, autoSubFlowConfig);
                    } else {
                        transitionTo(action, suspendedState.getState(), null, suspendedState);
                    }
                }
            } else {
                throw new FlowException("No suspended state to return to for terminating action " + action + " from state "
                        + applicationState.getCurrentContext().getState());
            }
            return true;
        } else {
            return false;
        }
    }

    protected void transitionToSubState(Action action, SubFlowConfig subStateConfig) {
        Class<?> subState = subStateConfig.getSubFlowConfig().getInitialState().getStateClass();
        transitionTo(action, createNewState(subState), subStateConfig, null);
    }

    protected Object createNewState(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new FlowException("Failed to create state " + clazz.getName(), ex);
        }
    }

    protected void transitionToState(Action action, Class<?> newStateClass) {
        StateConfig newStateConfig = applicationState.getCurrentContext().getFlowConfig().getStateConfig(newStateClass);
        if (newStateConfig != null) {
            transitionTo(action, newStateConfig);
        } else {
            throw new FlowException(String.format("No State found for class \"%s\"", newStateClass));
        }
    }

    protected void validateStateConfig(Object state, StateConfig stateConfig) {
        if (stateConfig == null) {
            throw new FlowException("No configuration found for state. \"" + state
                    + "\" This state needs to be mapped in a IFlowConfigProvider implementation. ");
        }
    }

    @Override
    public void endConversation() {
        applicationState.getScope().clearConversationScope();
        clearScopeOnStates(ScopeType.Conversation);
        clearScopeOnDeviceScopeBeans(ScopeType.Conversation);
        refreshDeviceScope();
    }

    void clearScopeOnStates(ScopeType scopeType) {
        List<StateContext> stack = applicationState.getStateStack();
        for (StateContext stateContext : stack) {
            Object state = stateContext.getState();
            injector.resetInjections(state, scopeType);
        }

        injector.resetInjections(applicationState.getCurrentContext().getState(), scopeType);
    }

    void clearScopeOnDeviceScopeBeans(ScopeType scopeType) {
        for (String name : new HashSet<>(applicationState.getScope().getDeviceScope().keySet())) {
            Object value = applicationState.getScopeValue(ScopeType.Device, name);
            injector.resetInjections(value, scopeType);
        }
    }

    @Override
    public void endSession() {
        applicationState.getScope().clearSessionScope();
        clearScopeOnStates(ScopeType.Session);
        clearScopeOnDeviceScopeBeans(ScopeType.Session);
        refreshDeviceScope();
    }


    public void setInitialFlowConfig(FlowConfig initialFlowConfig) {
        this.initialFlowConfig = initialFlowConfig;
    }

    @Override
    public void showToast(Toast toast) {
        keepAlive();

        if (applicationState.getCurrentContext() == null) {
            throw new FlowException(
                    "There is no applicationState.getCurrentContext() on this StateManager.  HINT: States should use @In(scope=ScopeType.Node) to get the StateManager, not @Autowired.");
        }

        if(toast.isPersistent() && toast.getPersistedId() == null) {
            throw new FlowException("Persistent toast message requires ID");
        }

        screenService.showToast(applicationState.getDeviceId(), toast);

        lastShowTimeInMs.set(System.currentTimeMillis());
    }

    @Override
    public void closeToast(Toast toast) {
        if (toast != null) {
            keepAlive();

            if (applicationState.getCurrentContext() == null) {
                throw new FlowException(
                        "There is no applicationState.getCurrentContext() on this StateManager.  HINT: States should use @In(scope=ScopeType.Node) to get the StateManager, not @Autowired.");
            }
            CloseToast closeToast = new CloseToast(toast.getPersistedId());
            screenService.closeToast(applicationState.getDeviceId(), closeToast);

            lastShowTimeInMs.set(System.currentTimeMillis());
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public void showScreen(UIMessage screen, Map<String, UIDataMessageProvider<?>> dataMessageProviderMap) {
        keepAlive();

        if (applicationState.getCurrentContext() == null) {
            throw new FlowException(
                    "There is no applicationState.getCurrentContext() on this StateManager.  HINT: States should use @In(scope=ScopeType.Node) to get the StateManager, not @Autowired.");
        }
        if (applicationState.getCurrentContext().getState() instanceof IMessageInterceptor) {
            ((IMessageInterceptor<UIMessage>) applicationState.getCurrentContext().getState()).intercept(
                    applicationState.getDeviceId(), screen);
        }

        if (screen != null) {
            ScreenConfig screenConfig = screensConfig.findScreenConfig(screen.getId());
            sessionTimeoutMillis = screenConfig != null && screenConfig.getTimeout() != null ?  screenConfig.getTimeout()*1000 : 0;
            sessionTimeoutAction = new Action(screenConfig != null && screenConfig.getTimeoutAction() != null ?  screenConfig.getTimeoutAction() : null);
        } else {
            sessionTimeoutMillis = 0;
            sessionTimeoutAction = null;
        }

        screenService.showScreen(applicationState.getDeviceId(), screen, dataMessageProviderMap);
        if (stateManagerObservers != null) {
            stateManagerObservers.onScreen(applicationState, screen); // log after because interceptors in screenService may modify the screen
        }

        lastShowTimeInMs.set(System.currentTimeMillis());
    }

    @Override
    public void showScreen(UIMessage screen) {
        showScreen(screen, null);
    }

    @Override
    public Device getDevice() {
        return new Device(
                applicationState.getAppId(),
                applicationState.getDeviceId()
        );
    }

    @Override
    public Device getParentDevice() {
        DeviceModel parentDevice = applicationState.getScopeValue(ScopeType.Device, "parentDevice");
        return parentDevice != null ? new Device(parentDevice.getAppId(), parentDevice.getDeviceId()) : null;
    }

    @Override
    public List<Device> getChildDevices() {
        List<DeviceModel> children = applicationState.getScopeValue(ScopeType.Device, "childDevices");
        return children != null
                ? children.stream().map(c -> new Device(c.getAppId(), c.getDeviceId())).collect(Collectors.toList())
                : Collections.emptyList();
    }

    @Override
    public String getDeviceMode() {
        return applicationState.getDeviceMode();
    }

    @Override
    public void setDeviceMode(String deviceMode) {
        applicationState.setDeviceMode(deviceMode);
    }

    // called from a Timer thread.
    public void checkSessionTimeout() {
        // TODO come back to this, not working well with queue based state manager..
        if (sessionTimeoutMillis > 0) {
            long inactiveMillis = System.currentTimeMillis() - lastInteractionTime.get().getTime();
            if (inactiveMillis > sessionTimeoutMillis) {
                sessionTimeout();
            }
        }
    }

    protected void sessionTimeout() {
        Action localSessionTimeoutAction = sessionTimeoutAction;
        localSessionTimeoutAction.setDoNotBlockForResponse(true);
        doAction(localSessionTimeoutAction);
    }

    @Override
    public ApplicationState getApplicationState() {
        return applicationState;
    }

    public void setApplicationState(ApplicationState applicationState) {
        this.applicationState = applicationState;
    }

    @Override
    public void registerQueryParams(Map<String, Object> queryParams) {
        if (queryParams != null) {
            if (queryParams.size() > 0) {
                log.info("Registering query params " + queryParams);
            }
            applicationState.getScope().setScopeValue(ScopeType.Device, "queryParams", queryParams);
        }
    }

    @Override
    public void registerPersonalizationProperties(Map<String, String> personalizationProperties) {
        if (personalizationProperties != null) {
            log.info("Registering personalization properties " + personalizationProperties.toString());
            personalizationProperties = UnmodifiableMap.unmodifiableMap(personalizationProperties);
        }
        applicationState.getScope().setScopeValue(ScopeType.Device, "personalizationProperties", personalizationProperties);
    }

    @Override
    public Map<String, String> getPersonalizationProperties() {
        return applicationState.getScopeValue(ScopeType.Device, "personalizationProperties");
    }

    protected void initClientContext() {
        ScopeValue scopeValue = getApplicationState().getScope().getScopeValue(ScopeType.Device, "personalizationProperties");
        if (scopeValue != null && scopeValue.getValue() != null) {
            Map<String, String> personalizationProperties = scopeValue.getValue();
            personalizationProperties.entrySet().forEach(entry -> clientContext.put(entry.getKey(), entry.getValue()));
        }
    }

    public Injector getInjector() {
        return injector;
    }

    public void setTransitionRestFlag(boolean transitionRestFlag) {
        this.transitionRestFlag.set(transitionRestFlag);
    }

    public void sendConfigurationChangedMessage() {
        String deviceId = applicationState.getDeviceId();

        Map<String, String> properties = getPersonalizationProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        if (! applicationState.getAppId().equals(properties.get("appId"))) {
            Map<String,String> updatedProperties = new HashMap<>(properties);
            updatedProperties.put("appId", applicationState.getAppId());
            registerPersonalizationProperties(updatedProperties);
        }

        List<String> additionalTags = applicationState.getScopeValue("additionalTagsForConfiguration");

        try {
            if (clientConfigSelector != null) {
                Map<String, Map<String, String>> configs = clientConfigSelector.getConfigurations(properties, additionalTags);
                configs.forEach((name, clientConfiguration) -> messageService.sendMessage(deviceId,
                        new ClientConfigChangedMessage(name, clientConfiguration)));
            }

            // Send versions
            ClientConfigChangedMessage versionConfiguration = new ClientConfigChangedMessage("versions");
            versionConfiguration.put("versions", Versions.getVersions());
            messageService.sendMessage(deviceId, versionConfiguration);

        } catch (NoSuchBeanDefinitionException e) {
            log.info("An {} is not configured. Will not be sending client configuration to the client",
                    IClientConfigSelector.class.getSimpleName());
        }
    }

    protected void onEvent(Event event) {
        boolean offered = this.actionQueue.offer(new ActionContext(new Action(STATE_MANAGER_PROCESS_EVENT_ACTION, event)));
        if (!offered) {
            log.warn("StateManager failed to insert process event action into the queue");
        }
    }

}
