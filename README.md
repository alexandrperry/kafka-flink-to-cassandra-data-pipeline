# kafka-flink-to-cassandra-data-pipeline

### Requirements 
Docker or colima, sbt.

### Steps to run

1. Run docker or colima

``colima start --cpu 4 --memory 8 ``

2. Run kafka, zookeeper, kafka-ui, cassandra via docker-compose

`` docker-compose up -d ``

3. Wait some time when all be prepared

4. Connect to cassandra

`` docker exec -it kafka-flink-cassandra-cassandra-1  cqlsh -u cassandra -p cassandra ``

5. Create keyspace and table

`` CREATE KEYSPACE IF NOT EXISTS example WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}; ``

`` CREATE TABLE IF NOT EXISTS example.wordcount (word text,count bigint,PRIMARY KEY(word)); ``

6. Install deps and run project

`` sbt update && sbt run ``

### UI
[Kafka-ui](http://localhost:8080/)

[Flink](http://localhost:8081/)
