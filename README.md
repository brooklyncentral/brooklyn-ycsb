Brooklyn YCSB Entity
=======

Yahoo! Cloud Serving Benchmark ([YCSB](https://github.com/brianfrankcooper/YCSB/wiki)) is an extensible tool for benchmarking databases in a cloud environment.

This repository contains the YCSB Brooklyn entity for using the YCSB benchmark with other Brooklyn entities. The YCSB entity can be deployed within a composed Brooklyn blueprint to benchmark databases deployed and managed by [Brooklyn](https://brooklyn.incubator.apache.org/).

## Sample Deployment

```yaml
name: MySQL jdbc benchmarking with YCSB
location: jclouds:softlayer:ams01

services:
- serviceType: brooklyn.entity.database.mysql.MySqlNode
  id: db
  name: MySQL DB
  brooklyn.config:
    datastore.creation.script.url: classpath://ycsb-mysql-creation-script.sql

- serviceType: io.cloudsoft.ycsb.YCSBNode
  name: YCSB MySQL benchmarking Node
  brooklyn.config:
    dbToBenchmark: "jdbc"
    ycsbProperties:
      db.driver: "com.mysql.jdbc.Driver"
      db.url: $brooklyn:formatString("jdbc:%s%s",component("db").attributeWhenReady("datastore.url"), "ycsb")
      db.user: "brooklyn"
      db.passwd: "br00k11n"
    workloadFiles: ["classpath://workload-testa", "classpath://workload-testb"]
```

The YAML blueprint above specifies a single MySQL server node and a YCSB node to benchmark that server through JDBC. Other YAML examples are available in the blueprint.

----

Copyright 2013-2014 by Cloudsoft Corporation Limited

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
> 
> http://www.apache.org/licenses/LICENSE-2.0
> 
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.