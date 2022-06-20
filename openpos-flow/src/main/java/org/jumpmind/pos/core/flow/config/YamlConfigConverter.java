package org.jumpmind.pos.core.flow.config;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.jumpmind.pos.core.flow.*;
import org.jumpmind.pos.service.PosServerException;
import org.jumpmind.pos.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class YamlConfigConverter {

    private static final Logger log = LoggerFactory.getLogger(YamlConfigConverter.class);

    private static Map<String, Class<Object>> knownFlowClasses;

    private static Map<String, Class<Object>> knownOnEventClasses;

    private static Map<String, Class<Object>> overrideClassMap;

    private final static String GLOBAL_CONFIG = "Global";

    private List<String> additionalPackages;

    public YamlConfigConverter() {
    }

    public YamlConfigConverter(List<String> additionalPackages) {
        this.additionalPackages = additionalPackages;
    }

    public FlowConfig convertToFlowConfig(List<YamlFlowConfig> loadedYamlFlowConfigs, YamlFlowConfig yamlFlowConfig) {
        return convertFlowConfig(loadedYamlFlowConfigs, yamlFlowConfig);
    }

    public List<FlowConfig> convertToFlowConfig(List<YamlFlowConfig> yamlFlowConfigs) {
        List<FlowConfig> flowConfigs = new ArrayList<>();

        for (YamlFlowConfig yamlFlowConfig : yamlFlowConfigs) {
            FlowConfig flowConfig = convertFlowConfig(yamlFlowConfigs, yamlFlowConfig);
            flowConfigs.add(flowConfig);
        }

        return flowConfigs;
    }

    public List<FlowConfig> convertFlowConfigs(List<YamlFlowConfig> loadedYamlFlowConfigs, List<YamlFlowConfig> yamlFlowConfigs) {
        List<FlowConfig> flowConfigs = new ArrayList<>();
        for (YamlFlowConfig yamlFlowConfig : yamlFlowConfigs) {
            flowConfigs.add(convertFlowConfig(loadedYamlFlowConfigs, yamlFlowConfig));
        }

        return flowConfigs;
    }

    private FlowConfig convertFlowConfig(List<YamlFlowConfig> yamlFlowConfigs, YamlFlowConfig yamlFlowConfig) {
        FlowConfig flowConfig = new FlowConfig(yamlFlowConfig.getFlowName());

        yamlFlowConfig.getGlobalEventHandlers().forEach((s) -> {
            Class<?> clazz = resolveOnEventClass(s);
            if (clazz != null) {
                flowConfig.addEventHandler(clazz);
            } else {
                log.error("A global event handler was defined as {} but no matching class could be found", s);
            }
        });

        Map<String, YamlStateConfig> concreteStateConfigs = getConcreteStateConfigs(yamlFlowConfig);

        boolean initialState = true;

        for (String stateName : concreteStateConfigs.keySet()) {
            if (GLOBAL_CONFIG.equalsIgnoreCase(stateName)) {
                handleGlobalConfig(flowConfig, yamlFlowConfigs, concreteStateConfigs.get(stateName));
                continue;
            }

            StateConfig stateConfig = buildStateConfig(yamlFlowConfigs, concreteStateConfigs.get(stateName));
            if (initialState) {
                flowConfig.setInitialState(stateConfig);
                initialState = false;
            }
            flowConfig.add(stateConfig);
        }

        return flowConfig;
    }

    protected StateConfig buildStateConfig(List<YamlFlowConfig> yamlFlowConfigs, YamlStateConfig yamlStateConfig) {
        StateConfig stateConfig = new StateConfig();
        stateConfig.setStateName(yamlStateConfig.getStateName());

        Class<?> stateClass = resolveFlowClass(yamlStateConfig.getStateName(), true);

        if (stateClass != null) {
            stateConfig.setStateClass(resolveFlowClass(yamlStateConfig.getStateName(), true));
        } else {
            throw new FlowException("Failed to resolve state for name: \"" + yamlStateConfig.getStateName() +
                    "\". Check that a class named \"" + stateConfig.getStateName() + "\" exists and that it has an @OnArrive method, " +
                    "AND that it is under one of the following packages: org.jumpmind.pos " +
                    (!CollectionUtils.isEmpty(additionalPackages) ? " OR " + additionalPackages : ""));
        }

        stateConfig.setActionToStateMapping(buildActionToStateMapping(yamlFlowConfigs, yamlStateConfig));
        stateConfig.setActionToSubStateMapping(buildActionToSubStateMapping(yamlFlowConfigs, yamlStateConfig));
        return stateConfig;
    }

    protected void handleGlobalConfig(FlowConfig flowConfig, List<YamlFlowConfig> yamlFlowConfigs, YamlStateConfig yamlStateConfig) {
        Map<String, Class<?>> actionToStateMapping = buildActionToStateMapping(yamlFlowConfigs, yamlStateConfig);
        for (String actionName : actionToStateMapping.keySet()) {
            flowConfig.addGlobalTransitionOrActionHandler(actionName, actionToStateMapping.get(actionName));
        }

        Map<String, SubFlowConfig> actionToSubStateMapping = buildActionToSubStateMapping(yamlFlowConfigs, yamlStateConfig);
        for (String actionName : actionToSubStateMapping.keySet()) {
            FlowConfig subFlowConfig = actionToSubStateMapping.get(actionName).getSubFlowConfig();
            flowConfig.addGlobalSubTransition(actionName, subFlowConfig);
        }
    }

    protected Map<String, Class<?>> buildActionToStateMapping(List<YamlFlowConfig> yamlFlowConfigs, YamlStateConfig yamlStateConfig) {
        Map<String, Class<?>> actionToStateMapping = new LinkedHashMap<>();

        for (String actionName : yamlStateConfig.getActionToStateConfigs().keySet()) {
            YamlStateConfig stateConfig = yamlStateConfig.getActionToStateConfigs().get(actionName);
            if (!stateConfig.isSubTransition()) {
                Class<?> stateClass = resolveFlowClass(stateConfig.getStateName(), true);

                if (stateClass != null) {
                    actionToStateMapping.put(actionName, stateClass);
                } else {
                    throw new FlowException("Failed to resolve state for name: \"" + stateConfig.getStateName() +
                            "\". Check that a class named \"" + stateConfig.getStateName() + "\" exists and that it has an @OnArrive method, " +
                            "AND that it is under one of the following packages: org.jumpmind.pos " +
                            (!CollectionUtils.isEmpty(additionalPackages) ? " OR " + additionalPackages : ""));
                }
            }
        }

        return actionToStateMapping;
    }

    @SuppressWarnings("all")
    protected Map<String, SubFlowConfig> buildActionToSubStateMapping(List<YamlFlowConfig> yamlFlowConfigs, YamlStateConfig yamlStateConfig) {
        Map<String, SubFlowConfig> actionToSubStateMapping = new LinkedHashMap<>();

        for (String actionName : yamlStateConfig.getActionToStateConfigs().keySet()) {
            YamlStateConfig stateConfig = yamlStateConfig.getActionToStateConfigs().get(actionName);
            if (stateConfig.isSubTransition()) {
                SubFlowConfig subFlowConfig = convertSubflowReference(yamlFlowConfigs, actionName, stateConfig);
                actionToSubStateMapping.put(actionName, subFlowConfig);
            }
        }

        return actionToSubStateMapping;
    }

    private SubFlowConfig convertSubflowReference(List<YamlFlowConfig> allYamlFlowConfigs, String actionName, YamlStateConfig stateConfig) {
        SubFlowConfig subFlowConfig = new SubFlowConfig();

        YamlFlowConfig yamlFlowConfig = findFlowByName(stateConfig.getStateName(), allYamlFlowConfigs);
        if (yamlFlowConfig != null) {
            FlowConfig flowConfig = convertToFlowConfig(allYamlFlowConfigs, yamlFlowConfig);
            subFlowConfig.setSubFlowConfig(flowConfig);
        } else {
            Class<?> stateClass = resolveFlowClass(stateConfig.getStateName(), false);
            if (stateClass == null) {
                throw new FlowException("Failed to resolve substate reference to a subflow or a state class: " +
                        stateConfig.getStateName() + " referred to by action: " + actionName);
            }

            YamlStateConfig yamlStateConfig =
                    findStatesByName(stateConfig.getStateName(), allYamlFlowConfigs).stream().filter(searchStateConfig ->
                    !searchStateConfig.getActionToStateConfigs().isEmpty()).findFirst().orElse(null);
            if (yamlStateConfig != null) {
                throw new PosServerException(String.format("Ambiguous subflow reference: '%s' (linked to action '%s'). " +
                        "A subflow should point to either " +
                        "a) another flow or b) an inline state that is not used other places. " +
                        "This subflow references a state that also has additional, external configuration. " +
                        "Instead of referring directly to a state, you should probably make a new " +
                        "flow and place the '%s' state in that new flow.",
                        yamlStateConfig.getStateName(), actionName, yamlStateConfig.getStateName()));
            }

            FlowConfig flowConfig = new FlowConfig(stateClass.getSimpleName());
            FlowBuilder builder = FlowBuilder.addState(stateClass);
            for (String returnAction : stateConfig.getReturnActions()) {
                builder.withTransition(returnAction, CompleteState.class);
            }
            flowConfig.setInitialState(builder.build());
            subFlowConfig.setSubFlowConfig(flowConfig);
        }

        subFlowConfig.setReturnActionNames(stateConfig.getReturnActions().toArray(new String[]{}));

        subFlowConfig.getSubFlowConfig().setConfigScope((Map) stateConfig.getConfigScope());

        return subFlowConfig;

    }

    protected YamlFlowConfig findFlowByName(String flowName, List<YamlFlowConfig> yamlFlowConfigs) {
        return yamlFlowConfigs.stream()
                .filter(yamlFlowConfig -> flowName.equals(yamlFlowConfig.getFlowName())).findAny().orElse(null);
    }

    protected List<YamlStateConfig> findStatesByName(String stateName, List<YamlFlowConfig> yamlFlowConfigs) {
        return yamlFlowConfigs.stream().flatMap(yamlFlowConfig -> yamlFlowConfig.getFlowStateConfigs().stream()).filter(yamlStateConfig ->
                yamlStateConfig.getStateName().equals(stateName)).collect(Collectors.toList());
    }

    protected Class<?> resolveFlowClass(String name, boolean allowGlobalActionHandler) {
        if (knownFlowClasses == null) {
            List<Class<Object>> knownFlowClassList = ClassUtils.getClassesForPackageAndType("org.jumpmind.pos", Object.class);
            if (additionalPackages != null) {
                additionalPackages.forEach(p -> knownFlowClassList.addAll(ClassUtils.getClassesForPackageAndType(p, Object.class)));
            }

            knownFlowClasses = knownFlowClassList.stream()
                    .filter(clazz -> filterStateClass(clazz, allowGlobalActionHandler))
                    .collect(Collectors.toMap(Class::getSimpleName, v -> v));
        }

        updateOverrideClasses();

        Class<?> stateClass = knownFlowClasses.get(name);
        Class<?> overrideClass = overrideClassMap.get(name);

        if (overrideClass != null) {
            stateClass = overrideClass;
        }

        return stateClass;
    }

    private void updateOverrideClasses() {
        if (overrideClassMap == null) {
            List<Class<?>> overrides = ClassUtils.getClassesForPackageAndAnnotation("org.jumpmind.pos", StateOverride.class);
            if (additionalPackages != null) {
                additionalPackages.forEach(p -> overrides.addAll(ClassUtils.getClassesForPackageAndAnnotation(p, StateOverride.class)));
            }

            //noinspection unchecked
            overrideClassMap = overrides.stream()
                    .collect(Collectors.toMap(
                            e -> e.getAnnotation(StateOverride.class).originalState().getSimpleName(),
                            v -> (Class<Object>) v)
                    );

            overrideClassMap.forEach((k, v) ->
                    log.info("Overriding original state: {} with new state: {}", k, v.getSimpleName()));
        }
    }

    protected Class<?> resolveOnEventClass(String simpleClassName) {
        if (knownOnEventClasses == null) {
            List<Class<Object>> knownOnEventClassesList = ClassUtils.getClassesForPackageAndType("org.jumpmind.pos", Object.class);
            if (additionalPackages != null) {
                additionalPackages.forEach(p -> knownOnEventClassesList.addAll(ClassUtils.getClassesForPackageAndType(p, Object.class)));
            }

            knownOnEventClasses = knownOnEventClassesList.stream()
                    .filter(FlowUtil::isEventHandler)
                    .collect(Collectors.toMap(Class::getSimpleName, v -> v));
        }

        return knownOnEventClasses.get(simpleClassName);

    }

    private boolean filterStateClass(Class<Object> clazz, boolean allowGlobalActionHandler) {
        if (allowGlobalActionHandler) {
            return (FlowUtil.isFlowClass(clazz)) || FlowUtil.isGlobalActionHandler(clazz);
        } else {
            return FlowUtil.isFlowClass(clazz);
        }
    }

    protected Map<String, YamlStateConfig> getConcreteStateConfigs(YamlFlowConfig yamlFlowConfig) {

        Map<String, YamlStateConfig> concreteStateConfigs = new LinkedHashMap<>();

        for (YamlStateConfig yamlStateConfig : yamlFlowConfig.getFlowStateConfigs()) {
            getConcreteStateConfigs(concreteStateConfigs, yamlStateConfig);
        }

        return concreteStateConfigs;
    }

    protected void getConcreteStateConfigs(Map<String, YamlStateConfig> concreteStateConfigs,
                                           YamlStateConfig yamlStateConfig) {
        if (yamlStateConfig.isConcreteStateDefinition()) {
            if (!concreteStateConfigs.containsKey(yamlStateConfig.getStateName())) {
                concreteStateConfigs.put(yamlStateConfig.getStateName(), yamlStateConfig);
                for (YamlStateConfig targetState : yamlStateConfig.getActionToStateConfigs().values()) {
                    getConcreteStateConfigs(concreteStateConfigs, targetState);
                }
            } else {
                if (!concreteStateConfigs.containsValue(yamlStateConfig)) {
                    throw new FlowException(String.format("State \"%s\"is defined conceretely (with actions) more than once. This is not currently supported. ", yamlStateConfig.getStateName()));
                }
            }
        }
    }

    public List<TransitionStepConfig> convertTransitionSteps(List<YamlTransitionStepConfig> yamlTransitionSteps, List<YamlFlowConfig> allYamlFlowConfigs) {
        if (yamlTransitionSteps == null || yamlTransitionSteps.isEmpty()) {
            return Collections.emptyList();
        }

        List<TransitionStepConfig> transitionSteps = new ArrayList<>(yamlTransitionSteps.size());

        for (YamlTransitionStepConfig yamlTransitionStep : yamlTransitionSteps) {
            TransitionStepConfig transitionStepConfig = new TransitionStepConfig();

            Class<?> flowClass = resolveFlowClass(yamlTransitionStep.getTransitionStepName(), true);

            if (FlowUtil.isTransitionStep((flowClass))) {
                //noinspection unchecked
                transitionStepConfig.setTransitionStepClass((Class<? extends ITransitionStep>) flowClass);

                for (String actionName : yamlTransitionStep.getActionToStateConfigs().keySet()) {
                    convertSubflowReference(allYamlFlowConfigs, actionName, yamlTransitionStep.getActionToStateConfigs().get(actionName));
                }
            } else {
                throw new FlowException("Class for name '" + yamlTransitionStep.getTransitionStepName() +
                        "' needs to implement ITransitionStep. Actual class: " + flowClass);
            }

            transitionSteps.add(transitionStepConfig);
        }

        return transitionSteps;

    }
}
