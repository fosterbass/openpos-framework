package org.jumpmind.pos.server.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Slf4j
@Configuration
public class SecuredServerConfig {

    @Bean
    public JettyServletWebServerFactory jettyEmbeddedServletContainerFactory(
            @Value("${server.port:6140}") final int port,
            @Value("${server.secondary.ports:}") final Set<Integer> secondaryPorts,
            @Value("${secured.enabled:false}") final boolean secureEnabled,
            @Autowired(required = false) SecuredConfiguration commerceServerSecuredConfiguration,
            @Autowired(required = false) SslContextFactory commerceServerSslContextFactory) {
        final JettyServletWebServerFactory factory =  new JettyServletWebServerFactory(port);

        // Add customized Jetty configuration with non blocking connection handler
        factory.addServerCustomizers(new JettyServerCustomizer() {
            @Override
            public void customize(final Server server) {
                // Register an additional connector for each secondary port.
                for(int secondaryPort : secondaryPorts) {
                    NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(server);
                    connector.setPort(secondaryPort);
                    server.addConnector(connector);
                }

                // Additional configuration

                if (secureEnabled) {
                    NetworkTrafficServerConnector securedConnector = new NetworkTrafficServerConnector(server, commerceServerSslContextFactory);
                    securedConnector.setPort(commerceServerSecuredConfiguration.getPort());
                    server.addConnector(securedConnector);
                    log.info("added secured connector for :{} (identified by \"{}\" from {})",
                            securedConnector.getPort(), commerceServerSslContextFactory.getCertAlias(), commerceServerSslContextFactory.getKeyStorePath());

                    for (int secondaryPort : commerceServerSecuredConfiguration.getSecondaryPorts()) {
                        NetworkTrafficServerConnector secondarySecuredConnector = new NetworkTrafficServerConnector(server, commerceServerSslContextFactory);
                        secondarySecuredConnector.setPort(secondaryPort);
                        server.addConnector(secondarySecuredConnector);
                    }
                }
            }
        });

        return factory;
    }

}
