package org.jumpmind.pos.service;

import org.apache.log4j.Logger;

//@Component
public class ModuleRegistry 
//implements BeanFactoryPostProcessor 
{

    private static Logger log = Logger.getLogger(ModuleRegistry.class);

//    @Autowired
//    private ConfigurableApplicationContext applicationContext;

//    private Map<String, ModuleDefinition> modules = new HashMap<>();

    // @EventListener(ContextRefreshedEvent.class)
//    @PostConstruct
//    public void initModules() {
//
//    }
    
//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        for (String beanName : beanFactory.getBeanDefinitionNames()) {
//            Object bean = beanFactory.getBean(beanName);
//            if (bean != null) {
//                checkAndRegisterModule(bean);
//            }
//        }
//
//        for (ModuleDefinition module : modules.values()) {
//            init(module, beanFactory);
//        }        
//    }      
    
//    protected void init(ModuleDefinition module, ConfigurableListableBeanFactory beanFactory) {
//
//        List<Class<?>> tableClasses = 
//                getClassesForPackageAndAnnotation(module.getModulePackage(), Table.class);
//
//        Map<String, String> sessionContext = PeristTestUtil.getSessionContext();
//        
//        DBSessionFactory sessionFactory = beanFactory.getBean(DBSessionFactory.class);
//        sessionContext.put("module.tablePrefix", module.getModule().getTablePrefix());
//        
//        // init sessionFactory per this module. 
//        sessionFactory.init(
//                PeristTestUtil.getH2TestProperties(), 
//                sessionContext, 
//                tableClasses,
//                null); // TODO
//        module.getModule().start();
//    }
//
//    private void checkAndRegisterModule(Object bean) {
//        if (bean instanceof Module) {
//            Module module = (Module) bean;
//            ModuleDefinition moduleDefinition = new ModuleDefinition();
//            moduleDefinition.setModule(module);
//            moduleDefinition.setModulePackage(bean.getClass().getPackage().getName());
//            modules.put(module.getName(), moduleDefinition);
//        }
//    }
//
//    protected List<Class<?>> getClassesForPackageAndAnnotation(String packageName, Class<? extends Annotation> annotation) {
//        List<Class<?>> classes = new ArrayList<Class<?>>();
//        ClassPathScanningCandidateComponentProvider scanner =
//                new ClassPathScanningCandidateComponentProvider(true);
//        scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
//        for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
//            try {
//                final Class<?> clazz = Class.forName(bd.getBeanClassName());
//                classes.add(clazz);
//            } catch (ClassNotFoundException ex) {
//                log.error(ex.getMessage());
//            }
//        }    
//        return classes;
//    }

  
}
