package io.cloudsoft.ycsb;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.nosql.riak.RiakCluster;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(YCSBRiakNodeImpl.class)
public interface YCSBRiakNode extends YCSBNode {

    @SetFromFlag("riakCluster")
    ConfigKey<RiakCluster> RIAK_CLUSTER = ConfigKeys.newConfigKey(RiakCluster.class, "ycsb.riak.cluster", "The Riak Cluster to be Benchmarked");

    @SetFromFlag("transport")
    ConfigKey<String> TRANSPORT = ConfigKeys.newStringConfigKey("ycsb.riak.transport","Transport to be used in YCSB (http|pb)","http");

    @SetFromFlag("dataProperty")
    ConfigKey<String> DATA_PROPERTY = ConfigKeys.newStringConfigKey("ycsb.riak.dataProperty","Where to store user data in user meta map or in value bytes array field (meta|value)","value");
}
