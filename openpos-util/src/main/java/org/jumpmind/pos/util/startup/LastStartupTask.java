package org.jumpmind.pos.util.startup;

import static java.lang.Integer.MAX_VALUE;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jumpmind.pos.util.BoxLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(MAX_VALUE)
public class LastStartupTask extends AbstractStartupTask {    

    private static Long startupTimestamp;

    @Autowired
    MutableBoolean initialized;
    
    @Value("${server.port:?}")
    String serverPort;

    @Value("${secured.port:?}")
    String securedPort;

    @Value("${secured.enabled:false}")
    boolean securedEnabled;

    @Override
    protected void doTask() throws Exception {
        initialized.setTrue();
        String msg = String.format("Server started on port %s (http)", serverPort);
        if (securedEnabled) {
            msg += String.format(" and on port %s (https)", securedPort);
        }

        if (startupTimestamp != null) {
            msg += String.format(".  It took %d seconds to startup", (int)((System.currentTimeMillis()-startupTimestamp)/1000));
        }

        logger.info(BoxLogging.box(msg));
    }

    public static void setStartupTimestamp() {
        if (startupTimestamp == null) {
            startupTimestamp = System.currentTimeMillis();
        }
    }
    
}
