ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-flink-cassandra",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    libraryDependencies ++= Seq(
      "org.apache.flink" %% "flink-scala" % "1.14.0",
      "org.apache.flink" %% "flink-streaming-scala" % "1.14.0",
      "org.apache.flink" %% "flink-connector-kafka" % "1.14.0",
      "org.apache.flink" %% "flink-clients" % "1.14.0",
      "org.apache.flink" %% "flink-connector-cassandra" % "1.14.0",
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.9.0",
      "org.apache.kafka" % "kafka-clients" % "2.8.0",
      "org.slf4j" % "slf4j-log4j12" % "1.7.36" % Test pomOnly(),
      "org.slf4j" % "slf4j-api" % "1.7.36",
      "org.slf4j" % "slf4j-nop" % "1.7.36" % Test,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.apache.flink" %% "flink-runtime-web" % "1.14.4",
      "org.apache.kafka" %% "kafka" % "2.8.1"
    )
  )


