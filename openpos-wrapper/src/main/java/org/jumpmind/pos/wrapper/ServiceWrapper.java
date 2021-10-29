package org.jumpmind.pos.wrapper;

import java.io.IOException;
import java.util.Scanner;

public class ServiceWrapper {

    protected static final String SYS_CONFIG_DIR = "org.jumpmind.pos.config.dir";
    protected static final String OPENPOS_HOME = "OPENPOS_HOME";

    public static void main(String[] args) throws Exception {
        System.out.println("THIS IS THE VERSION " + 2);

    	String configFileName = null;
    	if (args.length >= 2) {
    		configFileName = args[1];
    	} else {
    		configFileName = getFullyResolvedPath("/conf/openpos_service.conf");
    	}
        WrapperHelper.run(args, getHomeDir(), configFileName,  getFullyResolvedPath("/lib/openpos-wrapper.jar"));
    }


    static String convertStreamToString(java.io.InputStream is) throws IOException {
        Scanner scanner = new Scanner(is);
        java.util.Scanner s = scanner.useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        s.close();
        scanner.close();
        is.close();
        return result;
    }
    
    protected static String getFullyResolvedPath(String file) {
        String homeDir = getHomeDir();
        if (homeDir.endsWith("bin")) {
            return homeDir + "/.." + file;
        } else {
            return homeDir + file;
        }
    }

    protected static boolean isBlank(String value) {
        return value == null || value.trim().equals("");
    }
    
    protected static String getHomeDir() {
        String homeDir = System.getenv(OPENPOS_HOME);
        if (isBlank(homeDir)) {
            homeDir = System.getenv("SYM_HOME");
            if (isBlank(homeDir)) {
                homeDir = System.getProperty("user.dir");
            }
        }
        return homeDir;
    }
    
}
