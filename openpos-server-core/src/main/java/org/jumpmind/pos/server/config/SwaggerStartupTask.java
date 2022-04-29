package org.jumpmind.pos.server.config;

import org.jumpmind.pos.util.startup.AbstractStartupTask;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Optional;

import static java.lang.Integer.MAX_VALUE;

@Slf4j
@Component
@Order(MAX_VALUE - 100)
public class SwaggerStartupTask extends AbstractStartupTask {
    @Value("${server.port:6140}")
    private int appPort;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Override
    protected void doTask() {
        new Thread(() -> getApiDocsUrl().ifPresent(this::pingApiDocsUrl), "jmc.api-doc-load").start();
    }

    private Optional<URL> getApiDocsUrl() {
        final String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            return Optional.of(new URL("http", ip, appPort, apiDocsPath));
        }
        catch (UnknownHostException | MalformedURLException ex) {
            log.error("Error determining API documentation URL : {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private void pingApiDocsUrl(URL apiDocsUrl) {
        log.info("Pre-loading API documentation at URL [{}] to reduce first-access load time", apiDocsUrl);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) apiDocsUrl.openConnection();
            connection.setConnectTimeout(3_000);
            connection.setReadTimeout(60_000);
            connection.connect();

            /* If we don't consume the full content of the input stream, we'll eventually get a TimeoutException, even though our goal of preloading
             * the API documentation will have been achieved.  So read it all in. */
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.trace(line);
                }
            }

            log.info("API documentation loaded");
        }
        catch (IOException ex) {
            log.error("Error pre-loading API documentation : {}", ex.getMessage());
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
