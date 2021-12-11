package org.jumpmind.pos.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionsTest {

    @Test
    public void testFormatVersion() {
        Versions versions = new Versions();
        assertEquals(3001000, versions.formatVersion("3.1.0"));
        assertEquals(1010000, versions.formatVersion("1.10"));
        assertEquals(1010000, versions.formatVersion("1.10-SNAPSHOT"));
        assertEquals(202101111, versions.formatVersion("202.101.1111"));
        assertEquals(202101111, versions.formatVersion("202.101.1111.123123-SNAPSHOT"));
        assertEquals(3000000, versions.formatVersion("3.0.0-sfs-SNAPSHOT"));
    }
}
