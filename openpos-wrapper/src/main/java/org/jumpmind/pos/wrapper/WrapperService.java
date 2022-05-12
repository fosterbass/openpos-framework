package org.jumpmind.pos.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jumpmind.pos.wrapper.Constants.Status;

import org.update4j.*;
import org.update4j.service.UpdateHandler;

public abstract class WrapperService {

    private static final Logger logger = Logger.getLogger(WrapperService.class.getName());

    protected WrapperConfig config;

    protected boolean keepRunning = true;

    protected Process child;

    protected BufferedReader childReader;

    private static WrapperService instance;

    private static final String APPLICATION_NAME = "application";

    public synchronized static WrapperService getInstance() {
        if (instance == null) {
            if (SystemUtils.IS_OS_WINDOWS) {
                instance = new WindowsService();
            } else {
                instance = new UnixService();
            }
        }

        return instance;
    }

    public void loadConfig(String applHomeDir, String configFile, String jarFile) throws IOException {
        config = new WrapperConfig(applHomeDir, configFile, jarFile);
        setWorkingDirectory(config.getWorkingDirectory().getAbsolutePath());

        initLogging();
    }

    public void start() {
        if (isRunning()) {
            throw new WrapperException(Constants.RC_SERVER_ALREADY_RUNNING, 0, "Server is already running");
        }

        tryAutoUpdate();

        logger.info("Waiting for server to start");
        ArrayList<String> cmdLine = getWrapperCommand("exec");
        Process process = null;
        boolean success = false;
        int rc = 0;

        try {
            ProcessBuilder pb = new ProcessBuilder(cmdLine);
            pb.redirectErrorStream(true);
            process = pb.start();

            success = waitForPid(getProcessPid(process));
            if (!success) {
                rc = process.exitValue();
            }
        } catch (IOException e) {
            rc = -1;
            logger.info(e.getMessage());
        }

        if (success) {
            logger.info("Started");
        } else {
            try {
                if (process == null) {
                    logger.severe("failed to start process and unable to read process stdout");
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    logger.severe(line);
                }

                reader.close();
            } catch (Exception e) {
                logger.severe(() -> "failed to start process and an error occurred attempt to read the process stdout: " + e.getMessage());
            }

            throw new WrapperException(Constants.RC_FAIL_EXECUTION, rc, "Failed second stage");
        }
    }

    public void init() {
        initLogging();

        logger.info("initializing from service entrypoint");
        tryAutoUpdate();

        execJava(false);
    }

    public void console() {
        if (isRunning()) {
            throw new WrapperException(Constants.RC_SERVER_ALREADY_RUNNING, 0, "Server is already running");
        }
        execJava(true);
    }

    protected void initLogging() {
        try {
            LogManager.getLogManager().reset();

            final File logsFile = new File(config.getLogFile());

            Files.createDirectories(logsFile.getParentFile().toPath());

            final WrapperLogHandler handler = new WrapperLogHandler(
                    config.getLogFile(),
                    config.getLogFileMaxSize(),
                    config.getLogFileMaxFiles()
            );

            handler.setFormatter(new WrapperLogFormatter());

            final Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.parse(config.getLogFileLogLevel()));
            rootLogger.addHandler(handler);
        } catch (IOException e) {
            throw new WrapperException(Constants.RC_FAIL_WRITE_LOG_FILE, 0, "Cannot open log file " + config.getLogFile(), e);
        }
    }


    protected void execJava(boolean isConsole) {
        try {
            int pid = getCurrentPid();
            writePidToFile(pid, config.getWrapperPidFile());
            logger.info(() -> "Started wrapper as PID " + pid);

            ArrayList<String> cmd = config.getCommand(isConsole);
            String cmdString = commandToString(cmd);
            boolean usingHeapDump = cmdString.contains("-XX:+HeapDumpOnOutOfMemoryError");

            final String canonicalPath = config.getJavaProcessWorkingDirectory().getCanonicalPath();
            logger.info(() -> "Working directory is " + canonicalPath);

            long startTime = 0;
            int startCount = 0;
            boolean startProcess = true;
            boolean restartDetected = false;
            int serverPid = 0;

            while (keepRunning) {
                if (startProcess) {
                    logger.info(() -> "Executing " + cmdString);

                    if (startCount == 0) {
                        updateStatus(Status.START_PENDING);
                    }

                    startTime = System.currentTimeMillis();
                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.directory(config.getJavaProcessWorkingDirectory());
                    pb.redirectErrorStream(true);

                    try {
                        child = pb.start();
                    } catch (IOException e) {
                        logger.severe(() -> "Failed to execute: " + e.getMessage());
                        updateStatus(Status.STOPPED);
                        throw new WrapperException(Constants.RC_FAIL_EXECUTION, -1, "Failed executing server", e);
                    }

                    final int serverPidCapture =  getProcessPid(child);
                    logger.info(() -> "Started server as PID " + serverPidCapture);

                    serverPid = serverPidCapture;
                    writePidToFile(serverPid, config.getServerPidFile());

                    if (startCount == 0) {
                        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
                        updateStatus(Status.RUNNING);
                    }
                    startProcess = false;
                    startCount++;
                } else {
                    try {
                        logger.info("Watching output of java process");
                        childReader = new BufferedReader(new InputStreamReader(child.getInputStream()));
                        String line;

                        while ((line = childReader.readLine()) != null || child.isAlive()) {
                            if (line != null) {
                                logger.log(Level.INFO, line, "java");
                                if ((usingHeapDump && line.matches("Heap dump file created.*"))
                                        || (!usingHeapDump && line.matches("java.lang.OutOfMemoryError.*"))
                                        || line.matches(".*java.net.BindException.*")) {
                                    logger.log(Level.SEVERE, "Stopping server because its output matches a failure condition");
                                    child.destroy();
                                    childReader.close();
                                    stopProcess(serverPid, APPLICATION_NAME);
                                    break;
                                }
                                if (line.equalsIgnoreCase("Restarting")) {
                                    restartDetected = true;
                                }
                            }
                        }
                        logger.info("End of output from java process");
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error while reading from process");
                    }

                    if (restartDetected) {
                        logger.info("Restart detected");
                        restartDetected = false;
                        startProcess = true;
                    } else if (keepRunning) {
                        final int serverPidCapture = serverPid;
                        logger.severe(() -> "Unexpected exit from server: " + exitValue(serverPidCapture));

                        long runTime = System.currentTimeMillis() - startTime;

                        if (System.currentTimeMillis() - startTime < 7000) {
                            logger.severe(() -> "Stopping because server exited too quickly after only " + runTime + " milliseconds");
                            shutdown(Constants.RC_SERVER_EXITED);
                            throw new WrapperException(Constants.RC_SERVER_EXITED, exitValue(serverPid), "Unexpected exit from server");
                        } else {
                            startProcess = true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // The default logging config doesn't show the stack trace here, so
            // include it in the message.
            try {
                logger.severe(() -> "Exception caught.\r\n" + getStackTrace(ex));
                updateStatus(Status.STOPPED);
                throw new WrapperException(Constants.RC_SERVER_EXITED, child.exitValue(), "Exception caught.");
            } catch (Exception ex2) {
                ex.printStackTrace();
            }
        }
    }

    private int exitValue(int pid) {
        try {
            return child.exitValue();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Killing the child process explicitly because we could not get an exit value");
            child.destroy();
            stopProcess(pid, APPLICATION_NAME);
            try {
                return child.exitValue();
            } catch (Exception ex2) {
                logger.log(Level.SEVERE, "Failed to get the exit value for the process.  Returning 1");
                return 1;
            }
        }
    }

    public void stop() {
        int symPid = readPidFromFile(config.getServerPidFile());
        int wrapperPid = readPidFromFile(config.getWrapperPidFile());
        if (!isPidRunning(symPid) && !isPidRunning(wrapperPid)) {
            try {
                throw new WrapperException(Constants.RC_SERVER_NOT_RUNNING, 0, "Server is not running");
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        logger.info("Waiting for server to stop");
        if (!(stopProcess(wrapperPid, "wrapper") && stopProcess(symPid, APPLICATION_NAME))) {
            throw new WrapperException(Constants.RC_FAIL_STOP_SERVER, 0, "Server did not stop");
        }
        logger.info("Stopped");
    }

    protected boolean stopProcess(int pid, String name) {
        killProcess(pid, false);
        if (waitForPid(pid)) {
            killProcess(pid, true);
            if (waitForPid(pid)) {
                logger.info(() -> "ERROR: '" + name + "' did not stop");
                return false;
            }
        }
        return true;
    }

    protected void shutdown(int code) {
        if (keepRunning) {
            keepRunning = false;
            new Thread(() -> {
                logger.info("Stopping server");
                child.destroy();

                try {
                    childReader.close();
                } catch (IOException ignored) {
                }

                logger.info("Stopping wrapper");
                deletePidFile(config.getWrapperPidFile());
                deletePidFile(config.getServerPidFile());
                updateStatus(Status.STOPPED);
                System.exit(code);
            }).start();
        } else {
        	logger.info("Shutdown was requested, but it should have already been shutdown");
        }
    }

    public void restart() {
        if (isRunning()) {
            stop();
        }

        start();
    }

    public void relaunchAsPrivileged(String cmd, String args) {
    }

    public void status() {
        boolean isRunning = isRunning();
        logger.info(() -> "Installed: " + isInstalled());
        logger.info(() -> "Running: " + isRunning);
        if (isRunning) {
            logger.info(() -> "Wrapper PID: " + readPidFromFile(config.getWrapperPidFile()));
            logger.info(() -> "Server PID: " + readPidFromFile(config.getServerPidFile()));
        }
    }

    private void tryUpdateInstallCleanup() {
        logger.info("about to try cleanup");

        final File lockFile = config.getInstallLockFile();
        if (lockFile.exists()) {

            // using auto close feature + catch
            //noinspection EmptyTryBlock
            try (final FileChannel lockFileChannel = FileChannel.open(lockFile.toPath(), StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
                 final FileLock ignored = lockFileChannel.lock()
            ) {

            } catch (IOException ex) {
                logger.log(Level.WARNING, ex, () -> "failed to acquire update lock");
                return;
            }
        }

        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        final File[] files = tempDir.listFiles();

        if (files == null) {
            return;
        }

        Arrays.stream(files)
                .filter(file -> file.isDirectory() && file.getName().startsWith("libs-bak"))
                .forEach(file -> {
                    try {
                        final Boolean successfullyDeletedContents = Files.walk(file.toPath())
                                .map(subfile -> {
                                    try {
                                        Files.delete(subfile);
                                        return true;
                                    } catch (IOException e) {
                                        logger.warning(() ->"failed to delete file: " + e.getMessage());
                                        return false;
                                    }
                                })
                                .reduce((last, cur) -> last && cur)

                                // if it was `none` then there were likely no contents
                                .orElse(true);

                        if (Boolean.TRUE.equals(successfullyDeletedContents)) {
                            Files.delete(file.toPath());
                            logger.info(() -> "deleted lib backup: " + file.getName());
                        }
                    } catch (IOException e) {
                        logger.warning(() -> "failed to delete temp directory: " + e.getMessage());
                    }
                });
    }

    public void installUpdate(String updateFile) {
        try {
            if (isRunning()) {
                stop();
            }

            final File lockFile = config.getInstallLockFile();

            try(final FileChannel lockFileChannel = FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.DELETE_ON_CLOSE);
                final FileLock ignored = lockFileChannel.lock()
            ) {
                logger.fine(() -> "preparing to install update: '" + updateFile + "'");

                try {
                    Archive.read(updateFile).install();
                } catch (IOException ex) {
                    logger.severe(() -> "failed to install update at: " + updateFile + "; " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            logger.severe(() -> "cannot create lock file for update: " + ex.getMessage());
        } finally {
            start();
        }
    }

    public boolean isRunning() {
        return isPidRunning(getWrapperPid()) || isPidRunning(getServerPid());
    }

    public int getWrapperPid() {
        return readPidFromFile(config.getWrapperPidFile());
    }

    public int getServerPid() {
        return readPidFromFile(config.getServerPidFile());
    }

    protected String commandToString(ArrayList<String> cmd) {
        StringBuilder sb = new StringBuilder();
        for (String c : cmd) {
            sb.append(c).append(" ");
        }
        return sb.toString();
    }

    protected final ArrayList<String> getWrapperCommand(String command, String... additionalArgs) {
        return getWrapperCommandForOtherWrapper(config.getWrapperJarPath(), command, additionalArgs);
    }

    protected ArrayList<String> getWrapperCommandForOtherWrapper(String wrapperJar, String command, String... additionalArgs) {
        final ArrayList<String> cmd = new ArrayList<>();
        final String quote = getWrapperCommandQuote();

        cmd.add(quote + config.getJavaCommand() + quote);
        cmd.add("-Djava.io.tmpdir=" + quote + System.getProperty("java.io.tmpdir") + quote);
        cmd.add("-Duser.dir=" + quote + System.getProperty("user.dir") + quote);
        cmd.add("-jar");
        cmd.add(quote + wrapperJar + quote);
        cmd.add(command);
        cmd.add(quote + config.getConfigFile() + quote);

        cmd.addAll(Arrays.asList(additionalArgs));

        return cmd;
    }

    @SuppressWarnings("unused")
    protected ArrayList<String> getPrivilegedCommand() {
        ArrayList<String> cmd = new ArrayList<>();
        String quote = getWrapperCommandQuote();
        cmd.add(quote + config.getJavaCommand() + quote);
        return cmd;
    }

    protected String getWrapperCommandQuote() {
        return StringUtils.EMPTY;
    }

    protected int readPidFromFile(String filename) {
        int pid = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            pid = Integer.parseInt(reader.readLine());
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, e, () -> "failed to read process id from file; file '" + filename + "' was not found");
        } catch (IOException e) {
            logger.log(Level.WARNING, e, () -> "failed to read process id from file '" + filename + "'");
        }

        return pid;
    }

    protected void writePidToFile(int pid, String filename) {
        try {
            final File file = new File(filename);
            Files.createDirectories(file.getParentFile().toPath());

            try (FileWriter writer = new FileWriter(filename, false)) {
                writer.write(String.valueOf(pid));
            }
        } catch (IOException e) {
            logger.warning(() -> "failed to create process id lockfile for process '" + pid + "' at '" + filename + "': " + e.getMessage());
        }
    }

    protected void deletePidFile(String filename) {
        try {
            Files.delete(FileSystems.getDefault().getPath(filename));
        } catch (IOException e) {
            logger.warning(() -> "failed to delete process id lockfile at '" + filename + "': " + e.getMessage());
        }
    }

    protected boolean waitForPid(int pid) {
        logger.info(() -> "waiting for process '" + pid + "' to start");

        int seconds = 0;
        while (seconds <= 5) {
            if (!isPidRunning(pid)) {
                break;
            }

            logger.finest(() -> "process has not started yet; going to sleep for 1s");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            seconds++;
        }

        return isPidRunning(pid);
    }

    protected void updateStatus(Status status) {
    }

    private void doUpdateInstall(File updateFile) {
        tryUpdateInstallCleanup();

        final File jarFile = new File(WrapperService.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());

        final File jarDirectory = jarFile.getParentFile();

        final File libCpyDir;

        try {
            libCpyDir = Files.createTempDirectory("libs-bak").toFile();
        } catch (IOException ex) {
            logger.severe(() -> "failed to copy lib dir for update; could not create directory" + ex.getMessage());
            return;
        }

        // copy all the libs to a temp directory that we'll move execution over to.
        try (final Stream<Path> paths = Files.walk(jarDirectory.toPath())) {
            paths.forEach(f -> {
                final Path to = Paths.get(libCpyDir.toString(), f.toString().substring(jarDirectory.toString().length()));

                try {
                    Files.createDirectories(to);
                    Files.copy(f, to, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                    logger.warning(() -> "failed to copy file `" + f + "`");
                }
            });
        } catch (IOException ignored) {
            return;
        }

        try {
            final ArrayList<String> args = getWrapperCommandForOtherWrapper(
                    Paths.get(libCpyDir.getAbsolutePath(), jarFile.getName()).toString(),
                    "install-update",
                    getWrapperCommandQuote() + updateFile + getWrapperCommandQuote()
            );

            logger.info(() -> "starting out of process update");

            new ProcessBuilder()
                    .command(args)
                    .directory(config.getWorkingDirectory())
                    .start();
        } catch (IOException ex) {
            logger.warning(() -> "failed to start process" + ex.getMessage());
            return;
        }

        System.exit(0);
    }

    protected final void tryAutoUpdate() {
        if (config == null || !config.isAutoUpdateEnabled()) {
            logger.info("auto-update disabled; skipping...");
            return;
        }

        final File pendingUpdateFile = config.getPendingUpdateFile();

        if (pendingUpdateFile != null && pendingUpdateFile.exists()) {
            doUpdateInstall(pendingUpdateFile);
            return;
        }

        final String server = config.getAutoUpdateServer();
        if (StringUtils.isEmpty(server)) {
            logger.info("auto-update from server disabled; skipping...");
            return;
        }

        String businessUnitId = config.getBusinessUnitId();
        String packageName = config.getUpdatePackageName();

        if (StringUtils.isEmpty(businessUnitId)) {
            logger.info("missing business unit id");
            return;
        }

        if (StringUtils.isEmpty(packageName)) {
            logger.info("missing package name");
            return;
        }

        final String endpoint = StringUtils.stripEnd(server, "/") + "/update/manifest/" +  businessUnitId + "/" + packageName;
        final URL url;

        try {
            url = new URL(endpoint);
        } catch (MalformedURLException ex) {
            logger.severe(() -> "invalid url '" + endpoint + "'; cannot update from server...");
            return;
        }

        logger.info(() -> "searching for updates from '" + endpoint + "'");

        final URLConnection connection;

        try {
            connection = url.openConnection();
        } catch (IOException e) {
            logger.severe(() ->"failed to download update from server: " + e.getMessage());
            return;
        }

        connection.setConnectTimeout(10 * 1000);
        connection.setReadTimeout(10 * 1000);

        Configuration configuration;

        try {
            final Map<String, String> properties = config.properties.entrySet()
                    .stream()
                    .filter(s -> s.getValue().size() == 1)
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    o -> o.getValue().stream().findFirst().get()
                            )
                    );

            try (final InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                configuration = Configuration.read(reader, properties);
            }

            if (configuration.requiresUpdate()) {
                logger.info(() -> "update found; downloading update from '" + configuration.getBaseUri().toString() + "'");
            } else {
                logger.info("no update found");
                return;
            }
        } catch (IOException e) {
            logger.severe(() -> "failed to download update manifest: " + e.getMessage());
            return;
        }

        final Path tempFile;

        try {
            tempFile = Files.createTempFile("jmc", ".zip");

            logger.fine(() -> "creating temp file at '" + tempFile.toString() + "'");

            // let update4js create the file otherwise it won't download anything
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            logger.severe(() ->"failed to install update; cannot create temp file where update will be download to: " + e.getMessage());
            return;
        }

        final UpdateResult result = configuration.update(UpdateOptions.archive(tempFile).updateHandler(new UpdateHandler() {
            @Override
            public void startDownloadFile(FileMetadata file) {
                logger.fine(() -> "downloading update file '" + file.getPath() + "' with checksum '" + file.getChecksum() + "'");
            }

            @Override
            public void doneDownloadFile(FileMetadata file, Path path) {
                logger.fine(() -> "finished downloading file '" + file.getPath() + "'");
            }
        }));

        if (result.getException() != null) {
            logger.log(Level.SEVERE, result.getException(), () -> "failed to get update: " + result.getException().getMessage());
            return;
        }

        doUpdateInstall(tempFile.toFile());
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    class ShutdownHook extends Thread {
        @Override
        public void run() {
            shutdown(0);
        }
    }

    public abstract void install();

    public abstract void uninstall();

    public abstract boolean isInstalled();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean isPrivileged();

    @SuppressWarnings("UnusedReturnValue")
    protected abstract boolean setWorkingDirectory(String dir);

    protected abstract int getProcessPid(Process process);

    protected abstract int getCurrentPid();

    protected abstract boolean isPidRunning(int pid);

    protected abstract void killProcess(int pid, boolean isTerminate);

}
