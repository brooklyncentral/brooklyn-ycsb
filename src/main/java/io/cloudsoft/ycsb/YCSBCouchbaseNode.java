package io.cloudsoft.ycsb;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.nosql.couchbase.CouchbaseCluster;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(YCSBCouchbaseNodeImpl.class)
public interface YCSBCouchbaseNode extends YCSBNode {

    @SetFromFlag("couchbaseCluster")
    ConfigKey<CouchbaseCluster> COUCHBASE_CLUSTER = ConfigKeys.newConfigKey(CouchbaseCluster.class, "ycsb.couchbase.cluster", "The Couchbase Cluster to be Benchmarked");

    @SetFromFlag("bucket")
    ConfigKey<String> BUCKET = ConfigKeys.newStringConfigKey("ycsb.couchbase.bucket", "The Couchbase bucket name to be Benchmarked","default");

    @SetFromFlag("username")
    ConfigKey<String> USERNAME = ConfigKeys.newStringConfigKey("ycsb.couchbase.username", "The Couchbase username credential for Cluster access","Administrator");

    @SetFromFlag("password")
    ConfigKey<String> PASSWORD = ConfigKeys.newStringConfigKey("ycsb.couchbase.password", "The Couchbase password credential for Cluster access","Password");

    AttributeSensor<String> COUCHBASE_NODE_ADDRESSES = Sensors.newStringSensor("ycsb.couchbase.nodeAddresses", "The Couchbase Cluster nodes addresses list comma seperated");
}
