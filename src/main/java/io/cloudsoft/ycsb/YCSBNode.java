package io.cloudsoft.ycsb;

import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import brooklyn.config.ConfigKey;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.MethodEffector;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(YCSBNodeImpl.class)
public interface YCSBNode extends SoftwareProcess {

    @SetFromFlag("downloadUrl")
    BasicAttributeSensorAndConfigKey<String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey<String>(
            SoftwareProcess.DOWNLOAD_URL, "https://github.com/downloads/brianfrankcooper/YCSB/ycsb-0.1.4.tar.gz");

    @SetFromFlag("version")
    ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION,
            "0.1.4");

    ConfigKey<List<String>> DB_HOSTNAMES = ConfigKeys.newConfigKey(new TypeToken<List<String>>() {
    }, "ycsb.dbHostnames", "list of all hostnames to benchmark");
    ConfigKey<String> DB_TO_BENCHMARK = ConfigKeys.newStringConfigKey("ycsb.db_to_benchmark", "name of the db to benchmark", "basic");
    ConfigKey<Integer> THREADS = ConfigKeys.newIntegerConfigKey("ycsb.threads", "the number of client threads", 1);
    ConfigKey<Integer> TARGET = ConfigKeys.newIntegerConfigKey("ycsb.target", "Target ops/sec (default: unthrottled)", 0);

    ConfigKey<Map<String, String>> YCSB_PROPERTIES = ConfigKeys.newConfigKey(new TypeToken<Map<String, String>>() {
    }, "ycsb.properties", "any additional YCSB properties to use", Maps.<String, String>newHashMap());

    MethodEffector<Void> RUN_WORKLOAD = new MethodEffector<Void>(YCSBNode.class, "runWorkload");
    MethodEffector<Void> LOAD_WORKLOAD = new MethodEffector<Void>(YCSBNode.class, "loadWorkload");

    ConfigKey<String> LOCAL_OUTPUT_PATH = ConfigKeys.newStringConfigKey("ycsb.localOutputPath", "the path to fetch the output files to");
    ConfigKey<List<String>> WORKLOAD_FILES = ConfigKeys.newConfigKey(new TypeToken<List<String>>() {
    }, "ycsb.workloadFiles", "workload files to be copied to the machine", Lists.<String>newArrayList());

    @Effector(description = "Runs a workload on the database")
    void runWorkload(@EffectorParam(name = "run workload", description = "The name of the workload file") String workload);

    @Effector(description = "Loads a workload on the database")
    void loadWorkload(@EffectorParam(name = "load workload", description = "The name of the workload file") String workload);
}
