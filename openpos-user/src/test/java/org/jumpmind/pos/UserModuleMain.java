package org.jumpmind.pos;

import java.lang.reflect.Method;

import javax.print.attribute.standard.PrinterLocation;

import org.jumpmind.pos.user.model.User;
import org.jumpmind.pos.user.model.PasswordHistory;
import org.jumpmind.pos.user.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ComponentScan(
        basePackages = { "com.jumpmind.cst", "org.jumpmind.pos.core", "org.jumpmind.pos.app", "org.jumpmind.pos" })
@PropertySource(value = { "classpath:openpos-defaults.properties", "file:./conf/openpos.properties" }, ignoreResourceNotFound = true)
public class UserModuleMain {
    
    @Autowired
    private UserRepository userRepository;
    
    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            System.setProperty("spring.jackson.serialization.indent_output", "true");
            System.setProperty("openpos.development", "true");
            SpringApplication.run(UserModuleMain.class, args);
        } else if (args != null && args.length > 0) {
            Class<?> clazz = Class.forName(args[0]);
            Method method = clazz.getMethod("main", String[].class);
            String[] param1 = new String[0];
            method.invoke(clazz, new Object[] { param1});
        }
    }
    
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        createTestData();
    }

    private void createTestData() {
        {            
            User user = new User();
            user.setUsername("tstark");
            user.setFirstName("Anthony");
            user.setLastName("Stark");
            user.setGoesByName("Iron Man");
            
            PasswordHistory passwordHistory = new PasswordHistory();
            passwordHistory.setHashedPassword("stark"); // TODO
            user.addPasswordHistory(passwordHistory);
            
            userRepository.save(user);
        }
        {            
            User user = new User();
            user.setUsername("cmax");
            user.setFirstName("Crystalia");
            user.setLastName("Maximoff");
            user.setGoesByName("Crystal");
            userRepository.save(user);
        }
        {            
            User user = new User();
            user.setUsername("ajones");
            user.setFirstName("Angelica");
            user.setLastName("Jones");
            user.setGoesByName("Firestar");
            userRepository.save(user);
        }
        {            
            User user = new User();
            user.setUsername("jhowlett");
            user.setFirstName("James");
            user.setLastName("Howlett");
            user.setGoesByName("Wolverine");
            userRepository.save(user);
        }
        
//        ApplicationContext applicationContext = ((ContextRefreshedEvent) event).getApplicationContext();
//        System.out.println("Got it: " + applicationContext);
    }

}