package org.jumpmind.pos.update;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.exception.SecurityException;
import org.jumpmind.pos.persist.driver.Driver;
import org.jumpmind.pos.util.security.SelfSignedX509TrustManager;
import org.jumpmind.security.ISecurityService;
import org.jumpmind.security.SecurityServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.WebApplicationInitializer;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.jumpmind.pos.util.AppUtils.*;

@SpringBootApplication
@EnableScheduling
@ComponentScan(
        basePackages = {"org.jumpmind.pos"})
public class UpdateServer {

    @Autowired
    Environment environment;

    public static void main(String[] args) throws Exception {
        startupOutput();
        setupLogging("server");
        loadJumpMindDriver();
        configureSsl();
        loadCookieManager();

        new SpringApplicationBuilder().sources(UpdateServer.class).run(args);
    }

    static void startupOutput() {
        System.out.println("JumpMind Commerce Update Server startup at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) +
                " at path " + new File(".").getAbsolutePath());
        System.out.println("The classpath is: " + System.getProperty("java.class.path"));
        System.out.println("Logback configuration at: " + getLikelyLogConfigLocation());
    }

    static String getLikelyLogConfigLocation() {
        String configuredLocation = System.getProperty("logback.configurationFile");
        if (!StringUtils.isEmpty(configuredLocation)) {
            return configuredLocation;
        }

        configuredLocation = System.getProperty("logging.config");
        if (!StringUtils.isEmpty(configuredLocation)) {
            return configuredLocation;
        }

        URL logbackTestUrl = Thread.currentThread().getContextClassLoader().getResource("logback-test.xml");
        if (logbackTestUrl != null) {
            return logbackTestUrl.toString();
        }

        URL logbackUrl = Thread.currentThread().getContextClassLoader().getResource("logback.xml");
        if (logbackUrl != null) {
            return logbackUrl.toString();
        }

        return "unknown";
    }

    /**
     * AWS ELB ALB - needs to handle cookies for sticky sessions to work
     */
    static void loadCookieManager() {
        CookieHandler.setDefault(new CookieManager());
    }

    static void loadJumpMindDriver() {
        try {
            Class.forName(Driver.class.getName());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    static void configureSsl() {
        initSelfSignedSocketFactory();
    }

    static void initSelfSignedSocketFactory() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            ISecurityService securityService = SecurityServiceFactory.create();
            X509TrustManager trustManager = new SelfSignedX509TrustManager(null);
            context.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory sslSocketFactory = context.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        } catch (GeneralSecurityException ex) {
            throw new SecurityException(ex);
        }
    }
}
