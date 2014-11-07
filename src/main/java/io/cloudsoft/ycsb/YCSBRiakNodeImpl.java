package io.cloudsoft.ycsb;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.nosql.riak.RiakCluster;
import brooklyn.event.basic.DependentConfiguration;

public class YCSBRiakNodeImpl extends YCSBNodeImpl implements YCSBRiakNode {


    @Override
    public void init() {
        setConfig(DB_TO_BENCHMARK, "riak");
        setConfig(YCSBNode.DOWNLOAD_URL, "http://releng2.cloudsoftcorp.com/brooklyn/repository/YcsbNode/0.1.4/ycsb-0.1.4-altoros-riak.tar.gz");
        setConfig(YCSB_PROPERTIES.subKey("riak.transport"), getConfig(TRANSPORT));
        setConfig(YCSB_PROPERTIES.subKey("riak.dataproperty"), getConfig(DATA_PROPERTY));

        super.init();
    }

    @Override
    protected void postStart() {
        super.postStart();

        if (Optional.fromNullable(YCSBRiakNode.RIAK_CLUSTER).isPresent()) {
            RiakCluster cluster = getConfig(YCSBRiakNode.RIAK_CLUSTER);
            DependentConfiguration.waitInTaskForAttributeReady(cluster, Attributes.SERVICE_UP, Predicates.equalTo(Boolean.TRUE));

            addEnricher(Enrichers.builder()
                    .transforming(RiakCluster.NODE_LIST)
                    .computing(new Function<String, List<String>>() {

                        @Override
                        public List<String> apply(String nodeList) {
                            return Arrays.asList(nodeList.split(","));
                        }
                    })
                    .suppressDuplicates(true)
                    .from(cluster)
                    .publishing(HOSTNAMES)
                    .build());
        } else {
            throw new IllegalStateException(format("Couchbase Cluster configuration should be set to run the benchmark on node id:%s", getId()));
        }
    }
}
