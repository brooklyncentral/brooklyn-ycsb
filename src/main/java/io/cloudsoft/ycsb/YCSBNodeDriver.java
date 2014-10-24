package io.cloudsoft.ycsb;

import brooklyn.entity.basic.SoftwareProcessDriver;

public interface YCSBNodeDriver extends SoftwareProcessDriver {

    void runWorkload(String workload);

    void loadWorkload(String workload);

}