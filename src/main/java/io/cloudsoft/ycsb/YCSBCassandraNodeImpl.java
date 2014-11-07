package io.cloudsoft.ycsb;

import static java.lang.String.format;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import brooklyn.enricher.Enrichers;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.nosql.cassandra.CassandraNode;
import brooklyn.event.basic.DependentConfiguration;

public class YCSBCassandraNodeImpl extends YCSBNodeImpl implements YCSBCassandraNode {
    private static final Logger log = LoggerFactory.getLogger(YCSBCassandraNodeImpl.class);

    @Override
    protected void postStart() {
        super.postStart();
        if (Optional.fromNullable(getConfig(YCSBCassandraNode.CASSANDRA_DATACENTER)).isPresent()) {
            CassandraDatacenter cluster = getConfig(CASSANDRA_DATACENTER);
            Entities.waitForServiceUp(cluster);

            CassandraNode anyNode = (CassandraNode) Iterables.find(cluster.getMembers(), Predicates.instanceOf(CassandraNode.class));
            log.info("Creating keyspace 'usertable' with column family 'data' on Node {}", anyNode.getId());

            Entities.invokeEffectorWithArgs(this, anyNode, CassandraNode.EXECUTE_SCRIPT, "create keyspace usertable with placement_strategy = " +
                    "'org.apache.cassandra.locator.SimpleStrategy' and strategy_options = {replication_factor:1};" +
                    "\nuse usertable;" +
                    "\ncreate column family data;");

            setAttribute(YCSBCassandraNode.INIT_SCRIPT_EXECUTED, true);


                addEnricher(Enrichers.builder()
                        .transforming(CassandraDatacenter.CASSANDRA_CLUSTER_NODES)
                        .computing(Functions.<List<String>>identity())
                        .suppressDuplicates(true)
                        .from(cluster)
                        .publishing(HOSTNAMES)
                        .build());

        } else {
            throw new IllegalStateException(format("\"cassandraDatacenter\" configuration should be set to run the benchmark on node id:%s", getId()));
        }
    }

    @Override
    public void runWorkload(String workload) {
        if (Optional.fromNullable(getAttribute(HOSTNAMES)).isPresent()){
            super.runWorkload(workload);
        } else {
            throw new IllegalStateException("Cassandra Nodes hostnames to benchmark are not present");
        }
    }

    @Override
    public void loadWorkload(String workload) {
        if (Optional.fromNullable(getAttribute(HOSTNAMES)).isPresent()){
            super.loadWorkload(workload);
        } else {
            throw new IllegalStateException("Cassandra Nodes hostnames to benchmark are not present");
        }
    }

    @Override
    public void init() {
        setConfig(DB_TO_BENCHMARK, "cassandra-10");
        super.init();
    }
}