/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * http://www.gnu.org/licenses.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.pos.wrapper;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jumpmind.pos.wrapper.config.*;
import org.jumpmind.pos.wrapper.jna.WinsvcEx;
import static java.util.Arrays.*;

public class WrapperConfig {

	protected String configFile;
	
    protected Map<String, ArrayList<String>> properties;
    
    protected File workingDirectory;
    
    protected File javaProcessWorkingDirectory;

    protected String jarFile;

    protected Properties envProperties;

    protected final FunctionCollection functionCollection = BasicFunctionLibrary.collection();
    protected final IdentifierCollection identifierCollection;
    
    public WrapperConfig(String appHomeDir, String configFile, String jarFile) throws IOException {
        this.envProperties = this.loadEnvProperties();

        identifierCollection = new IdentifierCollection(
                envProperties.entrySet()
                        .stream()
                        .filter(set -> set.getKey() instanceof String)
                        .map(set -> IdentifierValue.fromConstant((String) set.getKey(), set.getValue()))
                        .collect(Collectors.toList())
        );

        this.properties = getProperties(configFile);
        this.configFile = new File(configFile).getAbsolutePath();
        this.jarFile = new File(jarFile).getAbsolutePath();
        this.workingDirectory = new File(appHomeDir);
    }

    public String getWrapperJarPath()  {
    	return jarFile;
    }

    public String getConfigFile() {
    	return configFile;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public String getLogFile() {
        return getProperty(properties, "wrapper.logfile", "logs/wrapper.log");
    }

    public String getWrapperPidFile() {
        return getProperty(properties, "wrapper.pidfile", "tmp/wrapper.pid");
    }

    public String getServerPidFile() {
        return getProperty(properties, "wrapper.server.pidfile", "tmp/server.pid");
    }

    public File getInstallLockFile() { return new File(getProperty(properties, "wrapper.updater.lockfile", "tmp/update.lock")); }

    public long getLogFileMaxSize() {
        String str = getProperty(properties, "wrapper.logfile.maxsize", "10M").toUpperCase();
        int multiplier = 1;
        if (str.indexOf("K") != -1) {
            multiplier = 1024;
        } else if (str.indexOf("M") != -1) {
            multiplier = 1048576;
        }
        return Long.parseLong(str.replaceAll("[^0-9]", "")) * multiplier;
    }

    public int getLogFileMaxFiles() {
        return Integer.parseInt(getProperty(properties, "wrapper.logfile.maxfiles", "3"));
    }

    public String getLogFileLogLevel() {
        return getProperty(properties, "wrapper.logfile.loglevel", "INFO");
    }

    public String getName() {
        return getProperty(properties, "wrapper.name", "symmetricds");
    }

    public String getDisplayName() {
        return getProperty(properties, "wrapper.displayname", "SymmetricDS");
    }

    public String getDescription() {
        return getProperty(properties, "wrapper.description", "SymmetricDS Database Synchronization");
    }

    public boolean isAutoStart() {
        return getProperty(properties, "wrapper.ntservice.starttype", "auto").equalsIgnoreCase("auto");
    }

    public boolean isDelayStart() {
        return getProperty(properties, "wrapper.ntservice.starttype", "auto").equalsIgnoreCase("delay");
    }

    public String getFailureActionCommand() {
        return getProperty(properties, "wrapper.ntservice.failure.action.command", "");
    }
    
    public int getFailureResetPeriod() {
        return Integer.parseInt(getProperty(properties, "wrapper.ntservice.failure.reset.period", "300"));
    }

    public List<FailureAction> getFailureActions() {
        List<String> types = getListProperty(properties, "wrapper.ntservice.failure.action.type");
        List<String> delays = getListProperty(properties, "wrapper.ntservice.failure.action.delay");
        int count = types.size() >= 3 ? 3 : types.size();
        List<FailureAction> actions = new ArrayList<FailureAction>(count);
        int i = 0;
        for (String type : types) {
            String delay = "0";
            if (i < delays.size()) {
                delay = delays.get(i); 
            }
            actions.add(new FailureAction(type, delay));
            i++;
        }
        return actions;
    }

    public List<String> getDependencies() {
        return properties.get("wrapper.ntservice.dependency");
    }
    
    public File getJavaProcessWorkingDirectory() {
        if (javaProcessWorkingDirectory == null) {
            String workDir = getProperty(properties, "wrapper.java.process.workingdir", this.getWorkingDirectory().getAbsolutePath());
            javaProcessWorkingDirectory = new File(workDir);
        }
        
        return javaProcessWorkingDirectory;
    }
    
    public String getJavaCommand() {
        return getProperty(properties, "wrapper.java.command", "java");
    }
    
    public List<String> getOptions() {
        return properties.get("wrapper.java.additional");
    }
    
    public List<String> getApplicationParameters() {
        return getListProperty(properties, "wrapper.app.parameter");
    }

    public ArrayList<String> getCommand(boolean isConsole) {
        ArrayList<String> cmdList = new ArrayList<String>();
        cmdList.add(getJavaCommand());

        String initMem = getProperty(properties, "wrapper.java.initmemory", "256");
        if (! initMem.toUpperCase().endsWith("M")) {
            initMem += "M";
        }        
        cmdList.add("-Xms" + initMem);

        String maxMem = getProperty(properties, "wrapper.java.maxmemory", "256");
        if (! maxMem.toUpperCase().endsWith("M")) {
            maxMem += "M";
        }        
        cmdList.add("-Xmx" + maxMem);
        
        String cpath = getClassPath();
        if (cpath != null && ! cpath.trim().isEmpty()) {
            cmdList.add("-cp");
            cmdList.add(cpath);
        }
        
        List<String> javaAdditional = getListProperty(properties, "wrapper.java.additional");
        cmdList.addAll(javaAdditional);
        
        List<String> appParams =  getListProperty(properties, "wrapper.app.parameter");
        appParams.remove("--no-log-console");
        cmdList.addAll(appParams);
        
        if (!isConsole) {
            cmdList.add("--no-log-console");
        }

        return cmdList;
    }

    public String getClassPath() {
        List<String> cp = getListProperty(properties, "wrapper.java.classpath");
        StringBuilder sb = new StringBuilder(cp.size());
        for (int i = 0; i < cp.size(); i++) {
            if (i > 0) {
                sb.append(File.pathSeparator);
            }
            String classPath = expandWildcard(cp.get(i));
            sb.append(classPath);
        }
        return sb.toString();
    }

    public boolean isAutoUpdateEnabled() {
        return getAutoUpdateServer() != null
                && getProperty(properties, "wrapper.updater.enabled", "false").equalsIgnoreCase("true");
    }

    public String getAutoUpdateServer() {
        return getProperty(properties, "wrapper.updater.server", null);
    }

    public File getPendingUpdateFile() {
        final String pendingFileLocation = getProperty(properties, "wrapper.updater.pendingfile", null);
        if (pendingFileLocation == null) {
            return null;
        }

        return new File(pendingFileLocation);
    }

    public String getInstallationId() {
        return getProperty(properties, "wrapper.installationid", null);
    }

    String expandWildcard(String classPath) {
        int index = classPath.indexOf("*");
        if (index != -1) {
            String dirName = classPath.substring(0, index);
            File dir = new File(dirName);
            if (dir.isDirectory()) {
                List<File> files = asList(dir.listFiles((directory, name) -> name.endsWith(".jar") || name.endsWith(".JAR")));
                files.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < files.size(); i++) {
                    if (i > 0) {
                        sb.append(File.pathSeparator);
                    }
                    sb.append(dirName).append(files.get(i).getName());
                }
                classPath = sb.toString();
            }
        }
        return classPath;
    }

    /**
     * Read wrapper properties from config file
     * 
     * @param filename String containing name location of the file
     * @return Map keyed by property name with value of an ArrayList containing all values
     * @throws IOException
     */
    Map<String, ArrayList<String>> getProperties(String filename) throws IOException {
        HashMap<String, ArrayList<String>> map = new HashMap<>();

        try (FileReader fileReader = new FileReader(filename); BufferedReader reader = new BufferedReader(fileReader)) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.matches("^\\s*#.*") && !line.matches("\\s*")) {
                    int index = line.indexOf("=");
                    if (index != -1) {
                        String name = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();

                        if (name.matches(".*\\d{1,2}")) {
                            name = name.substring(0, name.lastIndexOf("."));
                        }

                        ArrayList<String> values = map.computeIfAbsent(name, k -> new ArrayList<>());

                        values.add(doTokenReplacementOnValue(value));
                    }
                }
            }

            return map;
        }
    }

    Properties loadEnvProperties() {
        Properties properties = new Properties();
        Map<String, String> envs = System.getenv();
        for(String key : envs.keySet()) {
            properties.put(key, envs.get(key));
        }

        InputStream is = getClass().getResourceAsStream("/application-env.properties");
        if (is != null) {
            try {
                properties.load(is);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return properties;
    }

    private final Pattern expressionPattern = Pattern.compile("\\$\\{(?<expression>[\\w\\+\\-\\/\\*\\(\\)\\s\\'\\\"\\.\\,]+)\\}");
    String doTokenReplacementOnValue(String value) {
        final Matcher matcher = expressionPattern.matcher(value);

        while (matcher.find()) {
            final String expression = matcher.group("expression");

            final ConfigExpression compiledExpression;

            try {
                final ConfigExpressionLexer lexer = new ConfigExpressionLexer(new StringExpressionTextStream(expression));
                compiledExpression = ConfigExpression.parse(lexer, functionCollection, identifierCollection);
            } catch (Exception ex) {
                System.err.println("failed to compile expression '" + expression + "': " + ex.getMessage());
                continue;
            }

            final String result = compiledExpression.process();

            value = value.replace(matcher.group(), result);
        }

        return value;
    }
    
    String getProperty(Map<String, ArrayList<String>> prop, String name, String defaultValue) {
        ArrayList<String> values = prop.get(name);
        String value = null;
        if (values != null && !values.isEmpty()) {
            value = values.get(0);
        } else {
            value = defaultValue;
        }
        return value;
    }
    
    List<String> getListProperty(Map<String, ArrayList<String>> prop, String name) {
        ArrayList<String> value = prop.get(name);
        if (value == null) {
            value = new ArrayList<>(0);
        }
        return value;
    }
    
    public class FailureAction {
        int type;
        int delay;
        
        public FailureAction(String type, String delay) {
            if (type != null) {
                if (type.equalsIgnoreCase("restart")) {
                    this.type = WinsvcEx.SC_ACTION_RESTART;
                } else if (type.equalsIgnoreCase("reboot")) {
                    this.type = WinsvcEx.SC_ACTION_REBOOT;
                } else if (type.equalsIgnoreCase("run_command")) {
                    this.type = WinsvcEx.SC_ACTION_RUN_COMMAND;
                } else {
                    this.type = WinsvcEx.SC_ACTION_NONE;
                }
            }
            this.delay = Integer.parseInt(delay);
        }
        
        public int getType() {
            return type;
        }
        
        public int getDelay() {
            return delay;
        }
    }
}
