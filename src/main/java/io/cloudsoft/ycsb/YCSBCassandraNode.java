package io.cloudsoft.ycsb;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(YCSBCassandraNodeImpl.class)
public interface YCSBCassandraNode extends YCSBNode {

    @SetFromFlag("cassandraDatacenter")
    ConfigKey<CassandraDatacenter> CASSANDRA_DATACENTER = ConfigKeys.newConfigKey(CassandraDatacenter.class,"ycsb.cassandra.node.cassandraDatacenter","The Cassandra Cluster to be Benchmarked");

    AttributeSensor<Boolean> INIT_SCRIPT_EXECUTED = Sensors.newBooleanSensor("ycsb.cassandra.node.initScriptExecuted","Inidicates whether the test keyspace has been created on the Cassandra cluster");
}
