package io.cloudsoft.ycsb.examples;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;

import brooklyn.catalog.Catalog;
import brooklyn.catalog.CatalogConfig;
import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.AbstractApplication;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.StartableApplication;
import brooklyn.entity.nosql.cassandra.CassandraDatacenter;
import brooklyn.entity.nosql.cassandra.CassandraNode;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.event.AttributeSensor;
import brooklyn.event.SensorEvent;
import brooklyn.event.SensorEventListener;
import brooklyn.event.basic.DependentConfiguration;
import brooklyn.event.basic.Sensors;
import brooklyn.launcher.BrooklynLauncher;
import brooklyn.util.CommandLineUtil;
import io.cloudsoft.ycsb.YCSBNode;

@Catalog(name = "YCSB Cassandra Cluster Benchmark", description = "A YCSB entity to benchmark a Cassandra Cluster")
public class YCSBCassandraBenchmarkApp extends AbstractApplication {

    @CatalogConfig(label = "The initial size of the Cassandra cluster to be benchmarked", priority = 1)
    public static final ConfigKey<Integer> CASSANDRA_CLUSTER_SIZE = ConfigKeys.newConfigKey(
            "cassandra.cluster.initialSize", "Initial size of the Cassandra cluster", 2);

    public static final AttributeSensor<Boolean> initScriptExecuted = Sensors.newBooleanSensor("scriptExecuted");
    public static final String DEFAULT_LOCATION_SPEC = "jclouds:aws-ec2:us-east-1";
    private static final Logger log = LoggerFactory.getLogger(YCSBCassandraBenchmarkApp.class);
    private static final AtomicBoolean scriptBoolean = new AtomicBoolean();
    private CassandraDatacenter cassandraCluster;

    public void initApp() {
        //initialize the Cassandra Cluster
        cassandraCluster = addChild(EntitySpec.create(CassandraDatacenter.class)
                .configure(CassandraDatacenter.CLUSTER_NAME, "Brooklyn")
                .configure(CassandraDatacenter.INITIAL_SIZE, getConfig(CASSANDRA_CLUSTER_SIZE))
                .configure(CassandraDatacenter.ENDPOINT_SNITCH_NAME, "GossipingPropertyFileSnitch")
                .configure(CassandraDatacenter.MEMBER_SPEC, EntitySpec.create(CassandraNode.class)));

        //create the benchmarking table on the Cassandra Cluster
        subscribeToMembers(cassandraCluster, CassandraDatacenter.SERVICE_UP, new SensorEventListener<Boolean>() {
            @Override
            public void onEvent(SensorEvent<Boolean> event) {
                if (Boolean.TRUE.equals(event.getValue()))
                    if (event.getSource() instanceof CassandraNode && scriptBoolean.compareAndSet(false, true)) {

                        CassandraNode anyNode = (CassandraNode) event.getSource();
                        log.info("Creating keyspace 'usertable' with column family 'data' on Node {}", event.getSource().getId());

                        Entities.invokeEffectorWithArgs(YCSBCassandraBenchmarkApp.this, anyNode, CassandraNode.EXECUTE_SCRIPT, "create keyspace usertable with placement_strategy = " +
                                "'org.apache.cassandra.locator.SimpleStrategy' and strategy_options = {replication_factor:3};" +
                                "\nuse usertable;" +
                                "\ncreate column family data;");

                        setAttribute(initScriptExecuted, true);
                    }
            }
        });

        //create the YCSB client to benchmark the cassandra cluster.
        //add ycsb properties
        Map<String, String> props = Maps.newHashMap();
        props.put("recordcount", "100000");
        props.put("measurementtype", "timeseries");

        //upload your own workload files
        List<String> workloadFiles = Lists.newArrayList("classpath://workload-testa", "classpath://workload-testb");

        addChild(EntitySpec.create(YCSBNode.class)
                .configure(YCSBNode.DB_TO_BENCHMARK, "cassandra-10")
                .configure(YCSBNode.DB_HOSTNAMES_LIST, DependentConfiguration.attributeWhenReady(cassandraCluster, CassandraDatacenter.CASSANDRA_CLUSTER_NODES))
                .configure(YCSBNode.YCSB_PROPERTIES, props)
                .configure(YCSBNode.WORKLOAD_FILES, workloadFiles));
    }

    public static void main(String[] argv) {
        List<String> args = Lists.newArrayList(argv);
        String port = CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
        String location = CommandLineUtil.getCommandLineOption(args, "--location", DEFAULT_LOCATION_SPEC);

        BrooklynLauncher launcher = BrooklynLauncher.newInstance()
                .application(EntitySpec.create(StartableApplication.class, YCSBCassandraBenchmarkApp.class)
                        .displayName("YCSB Cassandra Benchmark Test"))
                .webconsolePort(port)
                .location(location)
                .start();

        Entities.dumpInfo(launcher.getApplications());
    }
}
