package io.cloudsoft.ycsb;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.event.feed.ConfigToAttributes;
import brooklyn.location.Location;
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
    public void connectSensors() {
        connectedSensors = true;
        connectServiceUpIsRunning();
    }

    @Override
    protected void doStart(Collection<? extends Location> locations) {
        super.doStart(locations);

        ConfigToAttributes.apply(this, YCSBNode.DB_HOSTNAMES_LIST);
        ConfigToAttributes.apply(this, YCSBNode.DB_HOSTNAMES_STRING);
    }

    @Override
    public void runWorkload(String workload) {
        if (Optional.fromNullable(getAttribute(DB_HOSTNAMES_LIST)).isPresent() || Optional.fromNullable(getAttribute(DB_HOSTNAMES_STRING)).isPresent()) {
            YCSBNodeDriver driver = getDriver();
            driver.runWorkload(workload);
        } else {
            throw new IllegalArgumentException("DB Hostnames to benchmark are not ready");
        }
    }

    @Override
    public void loadWorkload(String workload) {
        if (Optional.fromNullable(getAttribute(DB_HOSTNAMES_LIST)).isPresent() || Optional.fromNullable(getAttribute(DB_HOSTNAMES_STRING)).isPresent()) {
            YCSBNodeDriver driver = getDriver();
            driver.loadWorkload(workload);
        } else {
            throw new IllegalArgumentException("DB Hostnames to benchmark are not ready");
        }
    }
}
