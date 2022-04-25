package org.jumpmind.pos.server.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.server.model.CachedMessage;
import org.jumpmind.pos.server.model.FetchMessage;
import org.jumpmind.pos.util.DefaultObjectMapper;
import org.jumpmind.pos.util.web.NotFoundException;
import org.jumpmind.pos.util.web.ServerException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Hidden
@CrossOrigin
@Controller
public class MessageService implements IMessageService {

    private final ObjectMapper mapper = DefaultObjectMapper.defaultObjectMapper();

    @Autowired
    private SimpMessagingTemplate template;

    @Value("${openpos.screens.jsonIncludeNulls:true}")
    private boolean jsonIncludeNulls;

    @Value("${openpos.general.websocket.sendBufferSizeLimit:8192000}")
    private int websocketSendBufferLimit;

    @Value("${openpos.general.message.cacheTimeout:300000}")
    private int messageCacheTimeout;

    @Autowired(required = false)
    private List<IActionListener> actionListeners;

    private Map<String, CachedMessage> cachedMessageMap;

    @PostConstruct
    public void init() {
        cachedMessageMap = Collections.synchronizedMap( new PassiveExpiringMap<>(messageCacheTimeout));

        if (!jsonIncludeNulls) {
            mapper.setSerializationInclusion(Include.NON_NULL);
        }
    }

    @GetMapping(path = "ping", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String ping() {
        log.info("Received a ping request");
        return "{ \"pong\": \"true\" }";
    }

    @MessageMapping("action/device/{deviceId}")
    public void action(@DestinationVariable String deviceId, @Payload Action action, Message<?> message) {
        if (action.getType() == null) {
            throw new ServerException("Message/action must have a type. " + message);
        }
        boolean handled = false;
        for (IActionListener actionListener : actionListeners) {
            if (action.getType() != null && actionListener.getRegisteredTypes() != null &&
                    actionListener.getRegisteredTypes().contains(action.getType())) {
                handled = true;
                actionListener.actionOccurred(deviceId, action);
            }
        }

        if (!handled) {
            throw new ServerException("Message/action was not handled by any action listeners. message=[" +
                    message + "] actionListeners=[" + actionListeners + "]");
        }
    }

    @Override
    public void sendMessage(String deviceId, org.jumpmind.pos.util.model.Message message) {
        try {
            StringBuilder topic = new StringBuilder(128);
            topic.append("/topic/app/device/").append(deviceId);

            String jsonString = messageToJson(message);

            byte[] json = jsonString.getBytes(UTF_8);

            if( json.length <= websocketSendBufferLimit ){
                this.template.send(topic.toString(), MessageBuilder.withPayload(json).build());
            } else {
                String id = UUID.randomUUID().toString();
                String fetchMessageJson = messageToJson(FetchMessage.builder().messageIdToFetch(id).build());
                cachedMessageMap.put(id, CachedMessage.builder().message(message).cachedTime(Date.from(Instant.now())).build());
                this.template.send(topic.toString(), MessageBuilder.withPayload(fetchMessageJson.getBytes(UTF_8)).build());
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to publish message for deviceId: " + deviceId + " " + message, ex);
        }
    }

    @GetMapping("api/app/device/{deviceId}/message/{id}")
    @ResponseBody
    public String getCachedMessage(@PathVariable("deviceId") String deviceId, @PathVariable("id") String id){
        try{
            if(cachedMessageMap.containsKey(id)){
                try {
                    org.jumpmind.pos.util.model.Message m = cachedMessageMap.get(id).getMessage();
                    cachedMessageMap.remove(id);
                    return messageToJson(m);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to fetch cached message" + id, e);
                }
            } else {
                throw new NotFoundException();
            }
        } catch (Exception e){
            Action errorAction = new Action("GlobalError", e);
            errorAction.setType("Screen");
            action(deviceId, errorAction, null);
            throw e;
        }
    }

    protected String messageToJson(org.jumpmind.pos.util.model.Message message) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
    }

}
