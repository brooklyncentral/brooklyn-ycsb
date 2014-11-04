package io.cloudsoft.ycsb;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.nosql.mongodb.sharding.MongoDBRouter;
import brooklyn.entity.nosql.mongodb.sharding.MongoDBRouterCluster;
import brooklyn.entity.nosql.mongodb.sharding.MongoDBShardedDeployment;
import brooklyn.event.basic.DependentConfiguration;

public class YCSBMongoDbNodeImpl extends YCSBNodeImpl implements YCSBMongoDbNode {

    @Override
    public void init() {
        //mongodb fix to use
        setConfig(DB_TO_BENCHMARK, "mongodb");
        setConfig(YCSBNode.DOWNLOAD_URL, "http://releng2.cloudsoftcorp.com/brooklyn/repository/YcsbNode/0.1.4/ycsb-0.1.4-mongodb-fix.tar.gz");
        setConfig(YCSB_PROPERTIES.subKey("mongodb.url"), DependentConfiguration.attributeWhenReady(this, MONGO_DB_ROUTER_URL));
        setConfig(YCSB_PROPERTIES.subKey("mongodb.database"), getConfig(MONGO_DB_NAME));

        super.init();
    }

    @Override
    protected void postStart() {
        super.postStart();

        if (Optional.fromNullable(MONGO_DB_DEPLOYMENT).isPresent()) {
            MongoDBShardedDeployment mongoDeployment = getConfig(MONGO_DB_DEPLOYMENT);
            DependentConfiguration.waitInTaskForAttributeReady(mongoDeployment, Attributes.SERVICE_UP, Predicates.equalTo(Boolean.TRUE));

            //get the mongo url from the router cluster (may use a replica set later on)
            addEnricher(Enrichers.builder()
                    .transforming(MongoDBShardedDeployment.ROUTER_CLUSTER)
                    .from(mongoDeployment)
                    .computing(new Function<MongoDBRouterCluster, String>() {
                        @Nullable
                        @Override
                        public String apply(@Nullable MongoDBRouterCluster routerCluster) {
                            String host = routerCluster.getAttribute(MongoDBRouterCluster.ANY_ROUTER).getAttribute(MongoDBRouter.HOSTNAME);
                            Integer port = routerCluster.getAttribute(MongoDBRouterCluster.ANY_ROUTER).getAttribute(MongoDBRouter.PORT);

                            return "mongodb://" + host + ":" + port;
                        }
                    })
                    .publishing(YCSBMongoDbNode.MONGO_DB_ROUTER_URL)
                    .build());
        }
    }

    @Override
    public void runWorkload(String workload) {
        if (Optional.fromNullable(getAttribute(MONGO_DB_ROUTER_URL)).isPresent()) {
            super.runWorkload(workload);
        } else {
            throw new IllegalStateException("MongoDb Router URL must be supplied to benchmark");
        }
    }

    @Override
    public void loadWorkload(String workload) {
        if (Optional.fromNullable(getAttribute(MONGO_DB_ROUTER_URL)).isPresent()) {
            super.loadWorkload(workload);
        } else {
            throw new IllegalStateException("MongoDb Router URL must be supplied to benchmark");
        }
    }
}
