package org.jumpmind.pos.wrapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WrapperConfigTest {

    @Test
    public void testTokenReplacement() throws Exception {
        WrapperConfig config = new WrapperConfig("./", "src/test/test.conf", "some.jar");
        assertEquals("replacement1", config.getDescription());
        assertEquals("replacement2", config.getDisplayName());
        assertEquals("${noReplacement}", config.getName());
    }
}
