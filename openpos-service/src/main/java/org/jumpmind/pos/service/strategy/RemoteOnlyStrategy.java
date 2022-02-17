package org.jumpmind.pos.service.strategy;

import lombok.Getter;
import lombok.Setter;
import org.jumpmind.pos.service.EndpointInvocationContext;
import org.jumpmind.pos.service.PosServerException;
import org.jumpmind.pos.service.ProfileConfig;
import org.jumpmind.pos.service.ServiceConfig;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.pos.util.model.ServiceException;
import org.jumpmind.pos.util.model.ServiceResult;
import org.jumpmind.pos.util.model.ServiceVisit;
import org.jumpmind.pos.util.status.Status;
import org.jumpmind.pos.util.web.ConfiguredRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_TOKEN_HEADER_NAME;

@Component(RemoteOnlyStrategy.REMOTE_ONLY_STRATEGY)
public class RemoteOnlyStrategy extends AbstractInvocationStrategy implements IInvocationStrategy {

    final Logger logger = LoggerFactory.getLogger(getClass());

    static final String REMOTE_ONLY_STRATEGY = "REMOTE_ONLY";

    @Autowired
    private ClientContext clientContext;

    @Autowired
    private ServiceConfig serviceConfig;

    @Autowired
    @Getter @Setter
    private RemoteProfileStatusMonitor statusMonitor;

    public String getStrategyName() {
        return REMOTE_ONLY_STRATEGY;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @PostConstruct
    protected void init() {
        serviceConfig.getProfiles().forEach( (profileId, profileConfig) ->
            statusMonitor.setStatusUrl(profileId, profileConfig.getUrl())
        );
    }

    @Override
    public Object invoke(EndpointInvocationContext endpointInvocationContext) throws Throwable {

        Throwable lastException = null;
        Object result = null;
        List<ServiceVisit> serviceVisits = new ArrayList<>();

        for (String profileId : endpointInvocationContext.getProfileIds()) {
            ServiceVisit serviceVisit = new ServiceVisit();
            serviceVisit.setProfileId(profileId);
            long startTime = System.currentTimeMillis();
            try {
                result = invokeProfile(profileId, endpointInvocationContext);
                break;
            } catch (Exception ex) {
                serviceVisit.setException(ex);
                lastException = ex;
                if (ex instanceof RemoteProfileOfflineException) {
                    logger.warn("Remote service '{}' is OFFLINE.", profileId);
                } else {
                    logger.warn(String.format("Remote service %s unavailable.", profileId), ex);
                }
            } finally {
                serviceVisit.setElapsedTimeMillis(System.currentTimeMillis()-startTime);
                serviceVisits.add(serviceVisit);
            }
        }

        if (result != null) {
            populateServiceVisits(result, serviceVisits);
            return result;
        }

        if (lastException != null) {
            ServiceException serviceException = new ServiceException("Failed to invoke remote service(s)", lastException);
            serviceException.setServiceVisits(serviceVisits);
            throw serviceException;
        }

        log.warn("We should not have gotten here - there should be a result or lastException");
        return null;
    }

    private void populateServiceVisits(Object result, List<ServiceVisit> serviceVisits) {
        if (result instanceof ServiceResult) {
            ((ServiceResult) result).setServiceVisits(serviceVisits);
        }
    }

    private HttpHeaders getHeaders(ProfileConfig profileConfig, String profileId) {
        HttpHeaders headers = new HttpHeaders();

        if (profileConfig.getApiToken() != null) {
            headers.set(REST_API_TOKEN_HEADER_NAME, profileConfig.getApiToken());
        } else {
            logger.warn("missing apiToken for service profile \"{}\"", profileId);
        }

        if( clientContext != null ) {
            clientContext.put("correlationId", UUID.randomUUID().toString());
            for (String propertyName : clientContext.getPropertyNames()) {
                headers.set("ClientContext-" + propertyName, clientContext.get(propertyName));
            }
        }
        return headers;
    }

    private Object invokeProfile(String profileId, EndpointInvocationContext endpointInvocationContext) throws ResourceAccessException {
        if (statusMonitor.isOffline(profileId)) {
            throw new RemoteProfileOfflineException(String.format("Remote profile '%s' is Offline, skipping service calls until service is back Online", profileId));
        }

        ProfileConfig profileConfig = serviceConfig.getProfileConfig(profileId);

        int httpTimeoutInSecond = profileConfig.getHttpTimeout();
        int connectTimeoutInSecond = profileConfig.getConnectTimeout() > 0 ? profileConfig.getConnectTimeout() : httpTimeoutInSecond;
        ConfiguredRestTemplate template = new ConfiguredRestTemplate(httpTimeoutInSecond, connectTimeoutInSecond);

        RequestMapping mapping = endpointInvocationContext.getMethod().getAnnotation(RequestMapping.class);
        RequestMethod[] requestMethods = mapping.method();

        HttpHeaders headers = getHeaders(profileConfig, profileId);

        if (requestMethods.length > 0) {
            try {
                Method method = endpointInvocationContext.getMethod();
                Object[] args = endpointInvocationContext.getArguments();
                HttpMethod requestMethod = translate(requestMethods[0]);
                String serverUrl = buildUrl(profileConfig, endpointInvocationContext);
                Object[] newArgs = findArgs(method, args);
                if (isMultiPartUpload(args)) {
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                    body.add(getRequestParamName(method), new FileSystemResource(getMultiPartFile(args)));
                    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                    ResponseEntity<String> response = template.postForEntity(serverUrl, requestEntity, String.class, newArgs);
                    if (response.getStatusCode() != HttpStatus.OK) {
                        throw new PosServerException();
                    }
                } else {
                    Object requestBody = findRequestBody(method, args);
                    if (method.getReturnType().equals(Void.TYPE)) {
                        template.execute(serverUrl, requestBody, requestMethod, headers, newArgs);
                    } else {
                        Object result = template.execute(serverUrl, requestBody, method.getReturnType(), requestMethod, headers, newArgs);
                        statusMonitor.setStatus(profileId, Status.Online);
                        return result;
                    }
                }
            } catch (ResourceAccessException rex) {
                statusMonitor.setStatus(profileId, Status.Offline, rex.getMessage());
                throw new RemoteProfileOfflineException(rex);
            } catch (Exception ex) {
                statusMonitor.setStatus(profileId, Status.Error, ex.getMessage());
                throw ex;
            }

        } else {
            throw new IllegalStateException("A method must be specified on the @RequestMapping");
        }

        return null;
    }

    protected String buildUrl(ProfileConfig profileConfig, EndpointInvocationContext endpointInvocationContext) {
        String url = profileConfig.getUrl();
        String path = buildPath(endpointInvocationContext.getProxy(), endpointInvocationContext.getMethod());
        url = String.format("%s%s", url, path);
        return url;
    }

    protected boolean isMultiPartUpload(Object[] args) {
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof MultipartFile) {
                    return true;
                }
            }
        }
        return false;
    }

    protected String getMultiPartFile(Object[] args) {
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof MultipartFile) {
                    return ((MultipartFile) arg).getName();
                }
            }
        }
        return null;
    }

    protected String getRequestParamName(Method method) {
        Annotation[][] types = method.getParameterAnnotations();
        for (int i = 0; i < types.length; i++) {
            Annotation[] argAnnotations = types[i];
            for (Annotation annotation : argAnnotations) {
                if (RequestParam.class.equals(annotation.annotationType())) {
                    return ((RequestParam) annotation).value();
                }
            }
        }
        return null;
    }

    protected Object[] findArgs(Method method, Object[] args) {
        List<Object> newArgs = new ArrayList<>();
        Annotation[][] types = method.getParameterAnnotations();
        for (int i = 0; i < types.length; i++) {
            Annotation[] argAnnotations = types[i];
            for (Annotation annotation : argAnnotations) {
                if (PathVariable.class.equals(annotation.annotationType()) || RequestParam.class.equals(annotation.annotationType())) {
                    newArgs.add(args[i]);
                }

            }
        }
        return newArgs.toArray();
    }

    protected Object findRequestBody(Method method, Object[] args) {
        Annotation[][] types = method.getParameterAnnotations();
        for (int i = 0; i < types.length; i++) {
            Annotation[] argAnnotations = types[i];
            for (Annotation annotation : argAnnotations) {
                if (RequestBody.class.equals(annotation.annotationType())) {
                    return args[i];
                }

            }
        }

        return null;
    }

    protected HttpMethod translate(RequestMethod method) {
        return HttpMethod.valueOf(method.name());
    }

}
