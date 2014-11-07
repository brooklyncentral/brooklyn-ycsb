package io.cloudsoft.ycsb;

import static java.lang.String.format;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.nosql.couchbase.CouchbaseCluster;
import brooklyn.event.basic.DependentConfiguration;

public class YCSBCouchbaseNodeImpl extends YCSBNodeImpl implements YCSBCouchbaseNode {
    @Override
    public void init() {
        //mongodb fix to use
        setConfig(DB_TO_BENCHMARK, "couchbase2.0");
        setConfig(YCSBNode.DOWNLOAD_URL, "http://releng2.cloudsoftcorp.com/brooklyn/repository/YcsbNode/0.1.4/ycsb-0.1.4-altoros-couchbase.tar.gz");
        setConfig(YCSB_PROPERTIES.subKey("couchbase.hosts"), DependentConfiguration.attributeWhenReady(this, COUCHBASE_NODE_ADDRESSES));
        setConfig(YCSB_PROPERTIES.subKey("couchbase.user"), getConfig(USERNAME));
        setConfig(YCSB_PROPERTIES.subKey("couchbase.password"), getConfig(PASSWORD));
        setConfig(YCSB_PROPERTIES.subKey("couchbase.bucket"), getConfig(BUCKET));
        setConfig(YCSB_PROPERTIES.subKey("couchbase.checkOperationStatus"), "true");

        super.init();
    }

    @Override
    protected void postStart() {
        super.postStart();

        if (Optional.fromNullable(COUCHBASE_CLUSTER).isPresent()) {
            CouchbaseCluster cluster = getConfig(COUCHBASE_CLUSTER);
            Entities.waitForServiceUp(cluster);

            addEnricher(Enrichers.builder()
                    .transforming(CouchbaseCluster.COUCHBASE_CLUSTER_UP_NODE_ADDRESSES)
                    .computing(new Function<List<String>, String>() {

                        @Override
                        public String apply(@Nullable List<String> strings) {
                            return getDriver().getHostnamesString(strings);
                        }
                    })
                    .suppressDuplicates(true)
                    .from(cluster)
                    .publishing(COUCHBASE_NODE_ADDRESSES)
                    .build());

        } else {
            throw new IllegalStateException(format("Couchbase Cluster configuration should be set to run the benchmark on node id:%s", getId()));
        }
    }

    @Override
    public void runWorkload(String workload) {
        if (Optional.fromNullable(COUCHBASE_NODE_ADDRESSES).isPresent()) {
            super.runWorkload(workload);
        } else {
            throw new IllegalStateException("Couchbase Cluster node addresses must be supplied to benchmark");
        }
    }

    @Override
    public void loadWorkload(String workload) {
        if (Optional.fromNullable(COUCHBASE_NODE_ADDRESSES).isPresent()) {
            super.loadWorkload(workload);
        } else {
            throw new IllegalStateException("Couchbase Cluster node addresses must be supplied to benchmark");

        }
    }
}
