package org.jumpmind.jumppos.core.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class SessionSubscribedListener implements ApplicationListener<SessionSubscribeEvent> {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    IStateManagerFactory stateManagerFactory;

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        String topicName = (String) event.getMessage().getHeaders().get("simpDestination");
        String nodeId = topicName.substring(topicName.indexOf("/node/") + "/node/".length());
        String appId = topicName.substring(topicName.indexOf("/app/") + "/app/".length(), topicName.indexOf("/node/"));
        logger.info("subscribed to {}", topicName);
        IStateManager stateManager = stateManagerFactory.retreive(appId, nodeId);
        if (stateManager == null) {
            stateManager = stateManagerFactory.create(appId, nodeId);
        } else {
            stateManager.refreshScreen();
        }

    }

}