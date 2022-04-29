package org.jumpmind.pos.core.service;

import org.jumpmind.pos.core.content.ContentProviderService;
import org.jumpmind.pos.core.error.IErrorHandler;
import org.jumpmind.pos.core.flow.*;
import org.jumpmind.pos.core.model.Form;
import org.jumpmind.pos.core.model.IDynamicListField;
import org.jumpmind.pos.core.model.IFormElement;
import org.jumpmind.pos.core.ui.CloseToast;
import org.jumpmind.pos.core.ui.IHasForm;
import org.jumpmind.pos.core.ui.Toast;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.data.UIDataMessageProvider;
import org.jumpmind.pos.core.util.LogFormatter;
import org.jumpmind.pos.server.model.Action;
import org.jumpmind.pos.server.service.IActionListener;
import org.jumpmind.pos.server.service.IMessageService;
import org.jumpmind.pos.util.DefaultObjectMapper;
import org.jumpmind.pos.util.web.MimeTypeUtil;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Hidden
@CrossOrigin
@Controller
public class ScreenService implements IScreenService, IActionListener {
    @Autowired
    LogFormatter logFormatter;

    private final ObjectMapper mapper = DefaultObjectMapper.defaultObjectMapper();

    @Autowired
    private IStateManagerContainer stateManagerContainer;

    @Value("${openpos.screens.jsonIncludeNulls:true}")
    private boolean jsonIncludeNulls;

    @Value("${openpos.ui.content.maxage:null}")
    private String contentMaxAge;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private UIDataMessageProviderService uiDataMessageProviderService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IErrorHandler errorHandler;

    @PostConstruct
    public void init() {
        if (!jsonIncludeNulls) {
            mapper.setSerializationInclusion(Include.NON_NULL);
        }
    }

    @SuppressWarnings("deprecation")
    @GetMapping("api/appId/{appId}/deviceId/{deviceId}/content")
    public void getImageAsByteArray(
            HttpServletResponse response,
            @PathVariable String appId,
            @PathVariable String deviceId,
            @RequestParam(name = "contentPath") String contentPath,
            @RequestParam(name = "provider") String provider) throws IOException {

        log.debug("Received a request for content: {}", contentPath);

        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        if (stateManager != null) {
            String contentType = MimeTypeUtil.getContentType(contentPath);
            response.setContentType(contentType);

            stateManagerContainer.setCurrentStateManager(stateManager);

            if(MimeTypeUtil.isContentTypeAudio(contentType)) {
                // Required by browsers to allow starting audio at arbitrary time
                response.setHeader("Accept-Ranges", "bytes");
            }

            if(StringUtils.isNotEmpty(contentMaxAge)){
                response.setHeader("Cache-Control", "max-age=" + contentMaxAge);
            }

            ContentProviderService contentProviderService = applicationContext.getBean(ContentProviderService.class);
            InputStream in = contentProviderService.getContentInputStream(contentPath, provider);
            ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();

            if (in != null) {
                try {
                    int byteCount = IOUtils.copy(in, tempOutputStream);

                    if(byteCount > -1) {
                        // Required by browsers to allow starting audio at arbitrary time
                        response.setContentLength(byteCount);
                    }

                    tempOutputStream.writeTo(response.getOutputStream());
                } finally {
                    IOUtils.closeQuietly(in);
                }
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
            stateManagerContainer.setCurrentStateManager(null);
        }
    }

    @GetMapping("api/app/{appId}/node/{deviceId}/control/{controlId}")
    @ResponseBody
    public String getComponentValues(
            @PathVariable String appId,
            @PathVariable String deviceId,
            @PathVariable String controlId,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            @RequestParam(name = "sizeLimit", defaultValue = "1000") Integer sizeLimit) {

        log.info("Received a request to load component values for {} {} {}", appId, deviceId, controlId);
        String result = getComponentValues(appId, deviceId, controlId, getLastScreen(deviceId), searchTerm, sizeLimit);

        if (result == null) {
            result = getComponentValues(appId, deviceId, controlId, getLastDialog(deviceId), searchTerm, sizeLimit);
        }
        return (result == null) ? "[]" : result;
    }

    private String getComponentValues(
            String appId,
            String deviceId,
            String controlId,
            UIMessage screen,
            String searchTerm,
            Integer sizeLimit) {

        String result = null;
        if (screen instanceof IHasForm) {
            IHasForm dynamicScreen = (IHasForm) screen;
            IFormElement formElement = dynamicScreen.getForm().getFormElement(controlId);

            List<String> valueList = null;
            if (formElement instanceof IDynamicListField) {
                valueList = ((IDynamicListField) formElement).searchValues(searchTerm, sizeLimit);
            }

            if (valueList != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    mapper.writeValue(out, valueList);
                } catch (IOException e) {
                    throw new RuntimeException("Error while serializing the component values.", e);
                }
                result = out.toString();

                log.info("Responding to request to load component values {} {} {} with {} values", appId, deviceId, controlId, valueList.size());
            } else {
                log.info("Unable to find the valueList for the requested component {} {} {}.", appId, deviceId, controlId);
            }
        }
        return result;
    }

    @Override
    public Collection<String> getRegisteredTypes() {
        return Arrays.asList("Screen", "KeepAlive");
    }

    @Override
    public void actionOccurred(String deviceId, Action action) {
        log.trace("actionOccurred -> deviceId: {}, action: {}", deviceId, action != null ? action.getName() : null);
        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        if (stateManager != null) {
            try {
                stateManagerContainer.setCurrentStateManager(stateManager);
                if (SessionTimer.ACTION_KEEP_ALIVE.equals(action.getName())) {
                    stateManager.keepAlive();
                } else if ("Refresh".equals(action.getName())) {
                    UIMessage lastDialog = getLastDialog(deviceId);
                    log.info("Received Refresh action from {}", deviceId);
                    showScreen(deviceId, getLastScreen(deviceId));
                    showScreen(deviceId, lastDialog);
                } else if ( uiDataMessageProviderService.handleAction(action, stateManager.getApplicationState())){
                    log.info("Action handled by UIMessageDataProvider from {}\n{}", deviceId, logFormatter.toJsonString(action));
                } else {
                    deserializeForm(stateManager.getApplicationState(), action);
                    action.setOriginatesFromDeviceFlag(true);

                    try {
                        log.debug("Posting action {}", action);
                        stateManager.doAction(action);
                    } catch (Throwable ex) {
                        if( errorHandler != null){
                            errorHandler.handleError(stateManager, ex);
                        } else {
                            log.error(String.format("Unexpected exception while processing action from %s: %s", deviceId, action), ex);
                            messageService.sendMessage(deviceId, Toast.createWarningToast(
                                    "The application received an unexpected error. Please report to the appropriate technical personnel"));
                        }
                    }
                }
            } finally {
                stateManagerContainer.setCurrentStateManager(null);
            }
        }
    }

    protected UIMessage removeLastDialog(String deviceId) {
        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        ApplicationState applicationState = stateManager != null ? stateManager.getApplicationState() : null;
        if (applicationState != null && applicationState.getLastDialog() != null) {
            UIMessage lastDialog = applicationState.getLastDialog();
            applicationState.setLastDialog(null);
            return lastDialog;
        } else {
            return null;
        }
    }

    @Override
    public UIMessage getLastDialog(String deviceId) {
        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        ApplicationState applicationState = stateManager != null ? stateManager.getApplicationState() : null;

        return (applicationState != null) ? applicationState.getLastDialog() : null;
    }

    @Override
    public UIMessage getLastScreen(String deviceId) {
        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        ApplicationState applicationState = stateManager != null ? stateManager.getApplicationState() : null;

        return (applicationState != null) ? applicationState.getLastScreen() : null;
    }

    @Override
    public UIMessage getLastPreInterceptedScreen(String deviceId) {
        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        ApplicationState applicationState = stateManager != null ? stateManager.getApplicationState() : null;

        return (applicationState != null) ? applicationState.getLastPreInterceptedScreen() : null;
    }

    @Override
    public UIMessage getLastPreInterceptedDialog(String deviceId) {
        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        ApplicationState applicationState = stateManager != null ? stateManager.getApplicationState() : null;

        return (applicationState != null) ? applicationState.getLastPreInterceptedDialog() : null;
    }

    @Override
    public void showToast(String deviceId, Toast toast) {
        interceptToast(deviceId, toast);
        messageService.sendMessage(deviceId, toast);
    }

    @Override
    public void closeToast(String deviceId, CloseToast toast) {
        interceptCloseToast(deviceId, toast);
        messageService.sendMessage(deviceId, toast);
    }

    @Override
    public void showScreen(String deviceId, UIMessage screen) {
        showScreen(deviceId, screen, null);
    }

    @Override
    public void showScreen(String deviceId, UIMessage screen, Map<String, UIDataMessageProvider<?>> uiDataMessageProviders) {
        IStateManager stateManager = stateManagerContainer.retrieve(deviceId, true);
        if (screen != null && stateManager != null) {
            ApplicationState applicationState = stateManager.getApplicationState();
            screen.setSequenceNumber(applicationState.incrementAndScreenSequenceNumber());

            UIMessage preInterceptedScreen = null;
            try {
                preInterceptedScreen = SerializationUtils.clone(screen);
                interceptScreen(deviceId, screen);
            } catch (Exception ex) {
                if (ex.toString().contains("org.jumpmind.pos.core.screen.ChangeScreen")) {
                    log.error(
                            "Failed to write screen to JSON. Verify the screen type has been configured by calling setType() on the screen object.",
                            ex);
                } else {
                    log.error("Failed to write screen to JSON", ex);
                }
            }

            uiDataMessageProviderService.updateProviders(applicationState, uiDataMessageProviders);
            messageService.sendMessage(deviceId, screen);

            if (screen.isDialog()) {
                applicationState.setLastDialog(screen);
                applicationState.setLastPreInterceptedDialog(preInterceptedScreen);
            } else if (!screen.getScreenType().equals("NoOp")) {
                applicationState.setLastScreen(screen);
                applicationState.setLastPreInterceptedScreen(preInterceptedScreen);
                applicationState.setLastDialog(null);
                applicationState.setLastPreInterceptedDialog(null);
            }
        }
    }

    protected void interceptToast(String deviceId, Toast toast) {
        String[] toastInterceptorBeanNames = applicationContext.getBeanNamesForType(ResolvableType.forClassWithGenerics(IMessageInterceptor.class, Toast.class));

        for (String beanName : toastInterceptorBeanNames) {
            @SuppressWarnings("unchecked")
            IMessageInterceptor<Toast> toastInterceptor = (IMessageInterceptor<Toast>) applicationContext.getBean(beanName);
            toastInterceptor.intercept(deviceId, toast);
        }
    }

    protected void interceptCloseToast(String deviceId, CloseToast closeToast) {
        String[] closeToastInterceptorBeanNames = applicationContext.getBeanNamesForType(ResolvableType.forClassWithGenerics(IMessageInterceptor.class, CloseToast.class));

        for (String beanName: closeToastInterceptorBeanNames) {
            @SuppressWarnings("unchecked")
            IMessageInterceptor<CloseToast> toastInterceptor =  (IMessageInterceptor<CloseToast>) applicationContext.getBean(beanName);
            toastInterceptor.intercept(deviceId, closeToast);
        }
    }

    protected void interceptScreen(String deviceId, UIMessage screen) {
        String[] screenInterceptorBeanNames = applicationContext.getBeanNamesForType(ResolvableType.forClassWithGenerics(IMessageInterceptor.class, UIMessage.class));

        for (String beanName : screenInterceptorBeanNames) {
            @SuppressWarnings("unchecked")
            IMessageInterceptor<UIMessage> screenInterceptor = (IMessageInterceptor<UIMessage>) applicationContext.getBean(beanName);
            screenInterceptor.intercept(deviceId, screen);
        }
    }

    protected void deserializeForm(ApplicationState applicationState, Action action) {
        if (hasForm(applicationState)) {
            try {
                Form form = mapper.convertValue(action.getData(), Form.class);

                if (form != null) {
                    // Sometimes Jackson convertValue method will produce an empty
                    // Form object even if the given action data doesn't even resemble a form!
                    if (Form.isAssignableFrom(action.getData())) {
                        action.setData(form);
                    } else {
                        log.trace("Given action data is not actually a form, is instance of {}",
                            action.getData() != null ? action.getData().getClass().getName() : "?");
                    }
                }
            } catch (IllegalArgumentException ex) {
                log.debug(ex.getMessage(), ex);
                // We should not assume a form will always be returned by
                // the DynamicFormScreen.
                // The barcode scanner can also return a value.
                // TODO: Allow serializing more than the form on an action.
            }
        }
    }

    protected boolean hasForm(ApplicationState applicationState) {
        return (applicationState.getLastDialog() != null)
                ? applicationState.getLastDialog() instanceof IHasForm
                : applicationState.getLastScreen() instanceof IHasForm;
    }

    protected void setFieldValue(Field field, Object target, Object value) {
        // TODO Validate this method is ever called.
        // TODO Move this logic to a utility method or delegate to one from here.
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            throw new FlowException("Field to set value " + value + " for field " + field + " from target " + target, ex);
        }
    }

    protected String getFieldValueAsString(Field field, Object target) {
        // TODO Validate this method is ever called.
        // TODO Move this logic to a utility method or delegate to one from here.
        try {
            field.setAccessible(true);
            Object value = field.get(target);

            return (value != null) ? String.valueOf(value) : null;
        } catch (Exception ex) {
            throw new FlowException("Field to get value for field " + field + " from target " + target, ex);
        }
    }
}
