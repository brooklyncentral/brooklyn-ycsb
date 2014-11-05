package io.cloudsoft.ycsb;

import java.util.List;

import brooklyn.entity.basic.SoftwareProcessDriver;

public interface YCSBNodeDriver extends SoftwareProcessDriver {

    void runWorkload(String workload);

    void loadWorkload(String workload);

    String fetchDBHostnames(List<String> hostnamesList);

}