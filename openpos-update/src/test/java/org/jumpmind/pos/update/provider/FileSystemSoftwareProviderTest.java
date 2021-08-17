package org.jumpmind.pos.update.provider;

import org.junit.Test;
import static org.junit.Assert.*;

public class FileSystemSoftwareProviderTest {

    @Test
    public void testParseVersionNumber4Digits() {
         FileSystemSoftwareProvider provider = new FileSystemSoftwareProvider();
         String version = provider.parseVersion("aeo-commerce-4.5.3.333.zip");
         assertEquals("4.5.3.333", version);
    }

    @Test
    public void testParseVersionNumber3Digits() {
        FileSystemSoftwareProvider provider = new FileSystemSoftwareProvider();
        String version = provider.parseVersion("filename-1.2.3.zip");
        assertEquals("1.2.3", version);
    }
}
