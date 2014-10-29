package io.cloudsoft.ycsb;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.EntityInternal;
import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.entity.drivers.downloads.DownloadResolver;
import brooklyn.entity.java.JavaSoftwareProcessSshDriver;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.os.Os;
import brooklyn.util.ssh.BashCommands;
import brooklyn.util.text.Strings;

public class YCSBNodeSshDriver extends JavaSoftwareProcessSshDriver implements YCSBNodeDriver {

    public String ycsbCommand = "";
    public String workloadDir = "";
    public String logsDir = "";
    private boolean mySqlClientInstalled = false;
    private String saveAs = null;
    private String mySqlClientSaveAs = null;

    public YCSBNodeSshDriver(SoftwareProcessImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public YCSBNodeImpl getEntity() {
        return (YCSBNodeImpl) entity;
    }


    @Override
    public void preInstall() {
        resolver = Entities.newDownloader(this);
        setExpandedInstallDir(getInstallDir());
    }

    @Override
    public void install() {
        List<String> urls = resolver.getTargets();
        saveAs = resolver.getFilename();

        ImmutableList.Builder<String> cmdsBuilder = ImmutableList.<String>builder()
                .add(BashCommands.INSTALL_TAR)
                .addAll(BashCommands.commandsToDownloadUrlsAs(urls, saveAs));


        //download the connectorJ (MySQL JDBC client) if MySQL is being benchmarked.
        if (entity.getConfig(YCSBNode.DB_TO_BENCHMARK).equals("jdbc") &&
                entity.getConfig(YCSBNode.YCSB_PROPERTIES).containsKey("db.driver") &&
                String.valueOf(entity.getConfig(YCSBNode.YCSB_PROPERTIES).get("db.driver")).equals("com.mysql.jdbc.Driver")) {
            String mySqlClientVersion = entity.getConfig(YCSBNode.MSYQL_CLIENT_VERSION);

            DownloadResolver mySqlClientResolver = ((EntityInternal) entity).getManagementContext().getEntityDownloadsManager()
                    .newDownloader(this, "mysqlClient", ImmutableMap.of("addonversion", mySqlClientVersion));

            List<String> mySqlClientUrls = mySqlClientResolver.getTargets();
            mySqlClientSaveAs = mySqlClientResolver.getFilename();
            cmdsBuilder.addAll(BashCommands.commandsToDownloadUrlsAs(mySqlClientUrls, mySqlClientSaveAs));
            mySqlClientInstalled = true;
        }


        newScript(INSTALLING)
                .body.append(cmdsBuilder.build())
                .execute();
    }

    @Override
    public void customize() {

        String ycsbHomeDir = Os.mergePaths(getRunDir(), "ycsb-" + getVersion());

        Map<String, String> copiedWorkloadFiles = Optional.fromNullable(entity.getConfig(YCSBNode.INSTALL_FILES)).or(Maps.<String, String>newHashMap());

        ImmutableList.Builder<String> commandsBuilder = ImmutableList.<String>builder()
                .add(format("mkdir -p %s", ycsbHomeDir))
                .add(format("cp %s/%s .", getInstallDir(), saveAs))
                .add(format("tar xvfz %s -C %s --strip-components 1", saveAs, ycsbHomeDir))
                .add(format("mkdir -p %s/logs", ycsbHomeDir));

        if (mySqlClientInstalled) {
            commandsBuilder.add(format("cp %s/%s .", getInstallDir(), mySqlClientSaveAs))
                    .add(format("tar xvfz %s -C %s", mySqlClientSaveAs, ycsbHomeDir));

        }
        //if workload files uploaded copy them to the workloads folder.
        if (!copiedWorkloadFiles.isEmpty()) {
            for (String workloadFile : copiedWorkloadFiles.values()) {
                commandsBuilder.add(format("cp %s %s/workloads", Os.mergePaths(getExpandedInstallDir(), workloadFile), ycsbHomeDir));
            }
        }

        newScript(CUSTOMIZING)
                .body.append(commandsBuilder.build())
                .execute();

        ycsbCommand = Os.mergePaths(ycsbHomeDir, "bin", "ycsb");
        workloadDir = Os.mergePaths(ycsbHomeDir, "workloads");
        logsDir = Os.mergePaths(ycsbHomeDir, "logs");
        entity.setAttribute(YCSBNode.YCSB_LOGS_PATH, logsDir);

    }

    @Override
    public void launch() {
        newScript(LAUNCHING)
                .body.append("pwd")
                .execute();
    }

    public String getDBHostnames() {
        Optional<List<String>> myHostnamesList = Optional.fromNullable(entity.getAttribute(YCSBNode.DB_HOSTNAMES_LIST));
        Optional<String> myHostnamesString = Optional.fromNullable(entity.getAttribute(YCSBNode.DB_HOSTNAMES_STRING));

        //remove port section from the hostname
        if (myHostnamesList.isPresent()) {

            List<String> dbHostnamesList = myHostnamesList.get();
            return Strings.join(Lists.newArrayList(Iterables.transform(dbHostnamesList, new Function<String, String>() {

                @Nullable
                @Override
                public String apply(@Nullable String s) {

                    if (s.contains(":")) {
                        int portIndex = s.indexOf(":");
                        return s.substring(0, portIndex);
                    } else
                        return s;
                }
            })), ",");
        } else {
            if (myHostnamesString.isPresent()) {
                String dbHostnamesString = myHostnamesString.get();

                if (dbHostnamesString.contains(":")) {
                    StringTokenizer tokenizer = new StringTokenizer(dbHostnamesString, ",");
                    StringBuilder hostsWithoutPorts = new StringBuilder();

                    while (tokenizer.hasMoreTokens()) {
                        String singleHost = tokenizer.nextToken();
                        if (tokenizer.hasMoreTokens()) {
                            hostsWithoutPorts.append(singleHost.substring(0, singleHost.lastIndexOf(":")) + ",");
                        } else {
                            hostsWithoutPorts.append(singleHost.substring(0, singleHost.lastIndexOf(":")));
                        }
                    }
                    return hostsWithoutPorts.toString();
                } else {
                    return dbHostnamesString;
                }
            }
            return "";
        }
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public void stop() {
    }

    @Override
    public void kill() {
        super.kill();
    }

    @Override
    protected String getLogFileLocation() {
        return null;
    }

    @Override
    protected Map getCustomJavaSystemProperties() {
        return super.getCustomJavaSystemProperties();
    }

    public void loadWorkload(String workload) {
        if (entity.getAttribute(Attributes.SERVICE_UP)) {

            long logId = getLogId().getAndIncrement();

            newScript("loadingWorkload")
                    .body.append(format("%s | tee %s/load-%s-%s.out", getLoadCmd(workload), logsDir, logId, workload))
                    .execute();
        }
    }

    public void runWorkload(String workload) {

        if (entity.getAttribute(Attributes.SERVICE_UP)) {

            long logId = getLogId().getAndIncrement();

            newScript("runningWorkload")
                    .body.append(format("%s | tee %s/run-%s-%s.out", getRunCmd(workload), logsDir, logId, workload))
                    .execute();
        }
    }

    private String getLoadCmd(String workload) {

        //String coreWorkloadClass = getEntity().getMainClass();
        String hostnames = getDBHostnames();
        String dbName = getDbName();
        Integer target = getTarget();
        Integer threads = getThreads();
        Map<String, Object> props = getProperties();

        StringBuilder loadcmd = new StringBuilder(format("%s load %s -s -target %s -threads %s -P %s/%s",
                ycsbCommand, dbName, target, threads, workloadDir, workload));

        if (!props.isEmpty()) {
            for (String property : props.keySet()) {
                loadcmd.append(format(" -p %s=%s", property, props.get(property)));
            }
        }

        loadcmd.append(format(" -p hosts=%s", hostnames));

        return loadcmd.toString();
    }

    private String getRunCmd(String workload) {

        String hostnames = getDBHostnames();
        String dbName = getDbName();
        Integer target = getTarget();
        Integer threads = getThreads();
        Map<String, Object> props = getProperties();

        StringBuilder runcmd = new StringBuilder(format("%s run %s -s -target %s -threads %s -P %s/%s",
                ycsbCommand, dbName, target, threads, workloadDir, workload));

        if (!props.isEmpty()) {
            for (String property : props.keySet()) {
                runcmd.append(format(" -p %s=%s", property, props.get(property)));
            }
        }

        runcmd.append(format(" -p hosts=%s", hostnames));

        return runcmd.toString();
    }

    private String getDB() {
        return entity.getConfig(YCSBNode.DB_TO_BENCHMARK);
    }

    private Integer getThreads() {
        return entity.getConfig(YCSBNode.THREADS);
    }

    private Integer getTarget() {
        return entity.getConfig(YCSBNode.TARGET);
    }

    private String getDbName() {
        return entity.getConfig(YCSBNode.DB_TO_BENCHMARK);
    }

    private AtomicLong getLogId() {
        return entity.getAttribute(YCSBNode.YCSB_LOGS_IDENTIFIER);
    }

    private Map<String, Object> getProperties() {
        return Optional.fromNullable(entity.getConfig(YCSBNode.YCSB_PROPERTIES)).or(Maps.<String, Object>newHashMap());
    }
}