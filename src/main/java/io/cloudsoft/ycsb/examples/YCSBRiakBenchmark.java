package io.cloudsoft.ycsb.examples;

import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;

import brooklyn.catalog.Catalog;
import brooklyn.catalog.CatalogConfig;
import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.AbstractApplication;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.StartableApplication;
import brooklyn.entity.nosql.riak.RiakCluster;
import brooklyn.entity.nosql.riak.RiakNode;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.event.basic.DependentConfiguration;
import brooklyn.launcher.BrooklynLauncher;
import brooklyn.util.CommandLineUtil;
import io.cloudsoft.ycsb.YCSBNode;

@Catalog(name = "YCSB Riak Benchmark", description = "A Blueprint to Benchmark a Riak Cluster using YCSB")
public class YCSBRiakBenchmark extends AbstractApplication {

    @CatalogConfig(label = "Riak Ring Size")
    public static final ConfigKey<Integer> RIAK_RING_SIZE = ConfigKeys.newConfigKey(
            "riak.ring.size", "Initial size of the Riak Ring", 2);

    public static String DEFAULT_LOCATION_SPEC = "jclouds:aws-ec2:us-east-1";

    @Override
    public void initApp() {
        RiakCluster cluster = addChild(EntitySpec.create(RiakCluster.class)
                .configure(RiakCluster.INITIAL_SIZE, getConfig(RIAK_RING_SIZE))
                .configure(RiakCluster.MEMBER_SPEC, EntitySpec.create(RiakNode.class)));

        List<String> workloadFiles = Lists.newArrayList("classpath://workload-testa", "classpath://workload-testb");

        Map<String, String> props = Maps.newHashMap();
        props.put("recordcount", "100000");
        props.put("measurementtype", "timeseries");

        String ycsbRiakdDownloadDriverUrl = "http://developers.cloudsoftcorp.com/brooklyn/repository/YcsbNode/0.1.4/ycsb-0.1.4-riak-driver.tar.gz";
        addChild(EntitySpec.create(YCSBNode.class)
                .configure(YCSBNode.DOWNLOAD_URL, ycsbRiakdDownloadDriverUrl)
                .configure(YCSBNode.DB_TO_BENCHMARK, "riak")
                .configure(YCSBNode.DB_HOSTNAMES_STRING, DependentConfiguration.attributeWhenReady(cluster, RiakCluster.NODE_LIST))
                .configure(YCSBNode.YCSB_PROPERTIES, props)
                .configure(YCSBNode.WORKLOAD_FILES, workloadFiles));
    }

    public static void main(String[] argv) {
        List<String> args = Lists.newArrayList(argv);
        String port = CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
        String location = CommandLineUtil.getCommandLineOption(args, "--location", DEFAULT_LOCATION_SPEC);

        BrooklynLauncher launcher = BrooklynLauncher.newInstance()
                .application(EntitySpec.create(StartableApplication.class, YCSBRiakBenchmark.class)
                        .displayName("YCSB Riak Benchmark Test"))
                .webconsolePort(port)
                .location(location)
                .start();

        Entities.dumpInfo(launcher.getApplications());
    }
}