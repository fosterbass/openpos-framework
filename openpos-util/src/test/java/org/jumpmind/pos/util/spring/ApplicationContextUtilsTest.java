package org.jumpmind.pos.util.spring;

import org.jumpmind.pos.UtilTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes= {UtilTestConfig.class})
public class ApplicationContextUtilsTest {

    @Test
    public void testInit() {
        assertNotNull(ApplicationContextUtils.instance().getApplicationContext());
    }
}