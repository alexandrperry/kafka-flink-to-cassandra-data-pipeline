import com.datastax.driver.core.Cluster
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.connectors.cassandra.{CassandraSink, ClusterBuilder}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig

import java.util.Properties
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object KafkaFlinkCassandra {
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true);

    val producerProps = new Properties()
    producerProps.put("bootstrap.servers", "localhost:9092")
    producerProps.put("acks", "all")
    producerProps.put("retries", "0")
    producerProps.put("batch.size", "16384")
    producerProps.put("linger.ms", "1")
    producerProps.put("buffer.memory", "33554432")
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val consumerProps = new Properties()
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "my_group")

    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.enableCheckpointing(10000)
    env.setParallelism(1);
    env.setRestartStrategy(RestartStrategies.failureRateRestart(10, Time.of(5, TimeUnit.MINUTES), Time.of(10, TimeUnit.SECONDS)))
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(5000)

    val kafkaConsumer = new FlinkKafkaConsumer[String]("my_favorite_topic", new SimpleStringSchema(), consumerProps)
    val kafkaStream = env.addSource(kafkaConsumer)

    import org.apache.flink.streaming.api.windowing.time.Time
    val batchStream = kafkaStream
      .flatMap(_.toLowerCase.split("\\s"))
      .filter(_.nonEmpty)
      .map((_, 1L))
      // group by the tuple field "0" and sum up tuple field "1"
      .keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(30)))
      .sum(1)

    val cassandraSink = CassandraSink.addSink(batchStream).setClusterBuilder(new ClusterBuilder {
      override def buildCluster(builder: Cluster.Builder) = builder.addContactPoint("localhost").withCredentials("cassandra", "cassandra").build()
    }).setQuery("INSERT INTO example.wordcount(word, count) values (?, ?);").build()

    val f = Future{
      import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
      val producer = new KafkaProducer[String, String](producerProps)

      for (w <- 0 to 100) {
        val record = new ProducerRecord[String, String]("my_favorite_topic", w.toString, f"hello${(w/6).floor.toString} world")
        producer.send(record)
        Thread.sleep(5000)
      }
    }

    env.execute("KafkaFlinkCassandra")

  }
}