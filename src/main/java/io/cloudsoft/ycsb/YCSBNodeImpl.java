package io.cloudsoft.ycsb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import brooklyn.entity.basic.SoftwareProcessImpl;

public class YCSBNodeImpl extends SoftwareProcessImpl implements YCSBNode {

    private static final Logger log = LoggerFactory.getLogger(YCSBNodeImpl.class);
    private final List<Integer> outputLoadIds = Lists.newArrayList();
    private final List<Integer> outputTransactionIds = Lists.newArrayList();

    @Override
    public void init() {
        super.init();
    }


    public void runWorkloadEffector(String workload) {

        if (Optional.fromNullable(getConfig(DB_HOSTNAMES)).isPresent()) {
            YCSBNodeDriver driver = getDriver();
            driver.runWorkload(workload);
        } else {
            throw new IllegalArgumentException("DB Hostnames to benchmark are not ready");
        }
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
}
