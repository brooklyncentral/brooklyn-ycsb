package io.cloudsoft.ycsb;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;
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
import brooklyn.event.basic.MapConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(YCSBNodeImpl.class)
public interface YCSBNode extends SoftwareProcess {

    @SetFromFlag("downloadUrl")
    BasicAttributeSensorAndConfigKey<String> DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey<String>(
            SoftwareProcess.DOWNLOAD_URL, "https://github.com/downloads/brianfrankcooper/YCSB/ycsb-${version}.tar.gz");

    @SetFromFlag("version")
    ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION,
            "0.1.4");

    @SetFromFlag("downloadAddonUrls")
    BasicAttributeSensorAndConfigKey<Map<String, String>> DOWNLOAD_ADDON_URLS = new BasicAttributeSensorAndConfigKey<Map<String, String>>(
            SoftwareProcess.DOWNLOAD_ADDON_URLS, ImmutableMap.of(
            "mysqlClient", "http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-${addonversion}.tar.gz"));

    @SetFromFlag("mysqlClientVersion")
    ConfigKey<String> MSYQL_CLIENT_VERSION = ConfigKeys.newStringConfigKey(
            "mysql.client.version", "Version of mysql Java client to be installed, if required", "5.1.33");
    /**
     * Configuring the hostnamesConfigList will override hostnameConfigString and will set "-p hosts= " property with a comma seperated
     * hostnames list stripped from 'http' or any prefixes and any ports
     */
    @SetFromFlag("hostnamesConfigList")
    ConfigKey<List<String>> HOSTNAMES_CONFIG_LIST = ConfigKeys.newConfigKey(new TypeToken<List<String>>() {
    }, "ycsb.hostnamesConfigList", "List of all hostnames to benchmark");

    @SetFromFlag("hostnamesConfigString")
    ConfigKey<String> HOSTNAMES_CONFIG_STRING = ConfigKeys.newStringConfigKey("ycsb.hostnamesConfigString", "Comma seperated string of all hostnames to benchmark");

    @SetFromFlag("dbToBenchmark")
    ConfigKey<String> DB_TO_BENCHMARK = ConfigKeys.newStringConfigKey("ycsb.db_to_benchmark", "Name of the db to benchmark", "basic");

    @SetFromFlag("threads")
    ConfigKey<Integer> THREADS = ConfigKeys.newIntegerConfigKey("ycsb.threads", "The number of client threads", 1);

    @SetFromFlag("target")
    ConfigKey<Integer> TARGET = ConfigKeys.newIntegerConfigKey("ycsb.target", "Target ops/sec (default: unthrottled)", 0);

    /**
     * Adding any YCSB Properties in the configuration map will override any properties specified in the workload files, as it will append
     * at the tail of the load|run call. (e.g. -p recordcount=100000 will override the same propety specified in the workload file.
     * Use this map only if you want to set global properties for all load|run calls.
     */
    @SetFromFlag("ycsbProperties")
    MapConfigKey<Object> YCSB_PROPERTIES = new MapConfigKey<Object>(Object.class, "ycsb.properties", "any additional YCSB properties to use");

    @SetFromFlag("workloadFiles")
    ConfigKey<List<String>> WORKLOAD_FILES = ConfigKeys.newConfigKey(new TypeToken<List<String>>() {
    }, "ycsb.workloadFiles", "workload files to be copied to the machine", Lists.<String>newArrayList());

    AttributeSensor<String> YCSB_LOGS_PATH = Sensors.newStringSensor("ycsb.logsPath", "The path for writing run/load benchmarking output logs");
    AttributeSensor<String> YCSB_COMMAND = Sensors.newStringSensor("ycsb.command", "The path for calling the 'ycsb' command script");
    AttributeSensor<String> YCSB_WORKLOADS_PATH = Sensors.newStringSensor("ycsb.workloadsPath", "The path for the ycsb workloads");
    AttributeSensor<AtomicLong> YCSB_LOGS_IDENTIFIER = Sensors.newSensor(AtomicLong.class, "ycsb.logsIdentifier", "An incrementing id to number load/run ycsb benchmarking logs");
    AttributeSensor<List<String>> HOSTNAMES = Sensors.newSensor(new TypeToken<List<String>>() {
    }, "ycsb.hostnames", "The list of the database hostnames to be benchmarked");

    MethodEffector<Void> RUN_WORKLOAD = new MethodEffector<Void>(YCSBNode.class, "runWorkload");
    MethodEffector<Void> LOAD_WORKLOAD = new MethodEffector<Void>(YCSBNode.class, "loadWorkload");

    @Effector(description = "Runs a workload on the database")
    void runWorkload(@EffectorParam(name = "run workload", description = "The name of the workload file") String workload);

    @Effector(description = "Loads a workload on the database")
    void loadWorkload(@EffectorParam(name = "load workload", description = "The name of the workload file") String workload);
}
