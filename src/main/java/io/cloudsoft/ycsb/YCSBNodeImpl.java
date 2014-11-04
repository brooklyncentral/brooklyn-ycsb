package io.cloudsoft.ycsb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.event.feed.ConfigToAttributes;
import brooklyn.util.os.Os;

public class YCSBNodeImpl extends SoftwareProcessImpl implements YCSBNode {

    @Override
    public void init() {
        super.init();

        List<String> workloadFiles = getConfig(WORKLOAD_FILES);
        if (!workloadFiles.isEmpty()) {
            Map<String, String> filesToBeCopied = Maps.<String, String>newHashMap();

            for (String localFile : workloadFiles) {
                filesToBeCopied.put(localFile, Os.mergePaths("workloads", localFile.substring(localFile.lastIndexOf('/') + 1)));
            }
            setConfig(INSTALL_FILES, filesToBeCopied);
        }

        setAttribute(YCSB_LOGS_IDENTIFIER, new AtomicLong(0));
    }

    @Override
    public Class getDriverInterface() {
        return YCSBNodeDriver.class;
    }

    @Override
    public YCSBNodeDriver getDriver() {
        return (YCSBNodeDriver) super.getDriver();
    }

    @Override
    protected void preStart() {
        Preconditions.checkNotNull(getDbName(), "The DB type to benchmark is not specified");

        if (getDbName().equals("jdbc")) {
            Map<String, Object> props = getProps();
            Preconditions.checkArgument(!props.isEmpty(), "YCSB JDBC properties should be set when benchmarking through JDBC");
            Preconditions.checkArgument(props.containsKey("db.driver"));
            Preconditions.checkArgument(props.containsKey("db.url"));
            Preconditions.checkArgument(props.containsKey("db.user"));
            Preconditions.checkArgument(props.containsKey("db.passwd"));
        }

    }

    @Override
    public void connectSensors() {
        connectedSensors = true;
        connectServiceUpIsRunning();
    }

    @Override
    protected void postStart() {
        super.postStart();
        ConfigToAttributes.apply(this, YCSBNode.DB_HOSTNAMES_LIST);
        ConfigToAttributes.apply(this, YCSBNode.DB_HOSTNAMES_STRING);
    }

    @Override
    public void runWorkload(String workload) {
        YCSBNodeDriver driver = getDriver();
        driver.runWorkload(workload);
    }

    @Override
    public void loadWorkload(String workload) {
        YCSBNodeDriver driver = getDriver();
        driver.loadWorkload(workload);
    }

    private String getDbName() {
        return getConfig(DB_TO_BENCHMARK);
    }

    private Map<String, Object> getProps() {
        return getConfig(YCSB_PROPERTIES);
    }
}
