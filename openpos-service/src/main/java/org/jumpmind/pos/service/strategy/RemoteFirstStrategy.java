package org.jumpmind.pos.service.strategy;

import org.jumpmind.pos.service.EndpointInvocationContext;
import org.jumpmind.pos.util.model.ServiceException;
import org.jumpmind.pos.util.model.ServiceResult;
import org.jumpmind.pos.util.model.ServiceVisit;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component(RemoteFirstStrategy.REMOTE_FIRST_STRATEGY)
@Slf4j
public class RemoteFirstStrategy implements IInvocationStrategy {
    static final String REMOTE_FIRST_STRATEGY = "REMOTE_FIRST";

    @Autowired
    @Qualifier("LOCAL_ONLY")
    private LocalOnlyStrategy localStrategy;

    @Autowired
    private RemoteOnlyStrategy remoteStrategy;

    @Override
    public String getStrategyName() {
        return REMOTE_FIRST_STRATEGY;
    }

    @Override
    public Object invoke(EndpointInvocationContext endpointInvocationContext) throws Throwable {
        try {
            return remoteStrategy.invoke(endpointInvocationContext);
        } catch (Throwable ex) {
            try {
                log.info("Remote service(s) unavailable. Trying local");

                long start = System.currentTimeMillis();
                Object result = localStrategy.invoke(endpointInvocationContext);
                long end = System.currentTimeMillis();

                if (result instanceof ServiceResult && ex instanceof ServiceException) {
                    ((ServiceResult) result).setServiceVisits(((ServiceException) ex).getServiceVisits());

                    ServiceVisit serviceVisit = new ServiceVisit();
                    serviceVisit.setElapsedTimeMillis(end - start);
                    serviceVisit.setProfileId("local");

                    ((ServiceResult) result).getServiceVisits().add(serviceVisit);
                }
                return result;
            } catch (Exception e) {
                log.error("Local service call failed - logging stack trace now and throwing original exception from remote service after this.", e);
                throw ex;
            }
        }
    }
}
