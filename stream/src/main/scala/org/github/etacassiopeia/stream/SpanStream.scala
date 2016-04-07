package org.github.etacassiopeia.stream

import kafka.serializer.DefaultDecoder
import org.apache.spark.SparkConf
import org.apache.spark.metrics.source.SparkInstrumentation
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * <h1>DirectKafkaStream</h1>
  * The DirectKafkaStream 
  *
  * @author Mohsen Zainalpour
  * @version 1.0
  * @since 7/04/16 
  */
object SpanStream {
  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("SpanStream")
    val ssc = new StreamingContext(sparkConf, Seconds(10))

    val instrumentation = new SparkInstrumentation("span.metrics");

    val kafkaMetric = ssc.sparkContext.accumulator(0l, "kafka messages consumed")
    instrumentation.source.registerAccumulator(kafkaMetric, "processRate")
    instrumentation.register()

    sparkConf.getAll.foreach(x => println(s"${x._1} ${x._2}"))
    val kafkaParams = Map[String, String]("metadata.broker.list" -> sparkConf.get("spark.metadata.broker.list"))

    val topicSet = sparkConf.get("spark.metadata.topic").split(",").toSet[String]

    val spans = KafkaUtils.createDirectStream[Array[Byte], Array[Byte], DefaultDecoder, DefaultDecoder](ssc, kafkaParams, topicSet)
    spans.foreachRDD(rdd => rdd.foreach(
      span => kafkaMetric += 1l
    ))

    ssc.start()
    ssc.awaitTermination()

  }
}
