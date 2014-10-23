package io.cloudsoft.ycsb;

import java.util.List;

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
            SoftwareProcess.DOWNLOAD_URL, "http://d3kbcqa49mib13.cloudfront.net/spark-${version}-bin-hadoop1.tgz");

    ConfigKey<List<String>> DB_HOSTNAMES = ConfigKeys.newConfigKey(new TypeToken<List<String>>() {
    }, "ycsb.dbHostnames", "list of all hostnames to benchmark");
    AttributeSensor<Integer> INSERT_START = Sensors.newIntegerSensor("ycsb.insertstart", "inital records number to start loading");
    AttributeSensor<Integer> INSERT_COUNT = Sensors.newIntegerSensor("ycsb.insertcount", "number of records the ycsb client is responsible for inserting");
    AttributeSensor<Integer> RECORD_COUNT = Sensors.newIntegerSensor("ycsb.recordcount", "the total number of records");
    AttributeSensor<Integer> OPERATIONS_COUNT = Sensors.newIntegerSensor("ycsb.operationcount", "the number of operations to to run on a database");
    ConfigKey<Boolean> TIMESERIES = ConfigKeys.newBooleanConfigKey("ycsb.timeseries", "flag to specify if timeseries to be calculated in results");
    ConfigKey<Integer> TIMESERIES_GRANULARITY = ConfigKeys.newIntegerConfigKey("ycsb.timseries.granularity", "time for intervals between timeseries averages");
    ConfigKey<String> DB_TO_BENCHMARK = ConfigKeys.newStringConfigKey("ycsb.db_to_benchmark", "name of the db to benchmark", "com.yahoo.ycsb.db.CassandraClient10");
    MethodEffector<Void> RUN_WORKLOAD = new MethodEffector<Void>(YCSBNode.class, "runWorkloadEffector");

    ConfigKey<String> LOCAL_OUTPUT_PATH = ConfigKeys.newStringConfigKey("ycsb.localOutputPath", "the path to fetch the output files to");

    @Effector(description = "Runs a workload on the database")
    void runWorkloadEffector(@EffectorParam(name = "workload", description = "The name of the workload file") String workload);

}
