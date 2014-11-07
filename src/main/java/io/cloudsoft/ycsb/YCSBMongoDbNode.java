package io.cloudsoft.ycsb;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.nosql.mongodb.sharding.MongoDBShardedDeployment;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(YCSBMongoDbNodeImpl.class)
public interface YCSBMongoDbNode extends YCSBNode {

    @SetFromFlag("mongoDbDeployment")
    ConfigKey<MongoDBShardedDeployment> MONGO_DB_DEPLOYMENT = ConfigKeys.newConfigKey(MongoDBShardedDeployment.class, "ycsb.mongodb.shardedDeployment", "The MongoDB Deployment to be Benchmarked");

//    @SetFromFlag("mongoDbUrl")
//    ConfigKey<String> MONGO_DB_URL = ConfigKeys.newStringConfigKey("ycsb.mongodb.mongoDbUrl", "The MongoDB Url to be Benchmarked", "mongodb://localhost:27017");
//
    @SetFromFlag("mongoDbName")
    ConfigKey<String> MONGO_DB_NAME = ConfigKeys.newStringConfigKey("ycsb.mongodb.mongoDbName", "The MongoDB Database name to be Benchmarked", "ycsb");

    AttributeSensor<String> MONGO_DB_ROUTER_URL = Sensors.newStringSensor("ycsb.mongodb.dbRouterUrl", "The router url the YCSB will use to benchmakr MongoDB");
}
