package com.github.etacassiopeia.trace.kafka

import java.util.Properties

import akka.actor.Actor
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

/**
  * <h1>KafkaProducerManager</h1>
  * The KafkaProducerManager 
  *
  * @author Mohsen Zainalpour
  * @version 1.0
  * @since 5/04/16 
  */
class KafkaProducerManager(config: Config) extends Actor {

  private val topic = config.getString("topic")

  private val kafkaProducerConfig = {
    val props = new Properties()
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("bootstrap-server-list"))
    props.setProperty(ProducerConfig.ACKS_CONFIG, config.getInt("request-required-acks").toString)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("key-serializer-class"))
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("value-serializer-class"))
    props.put(ProducerConfig.RETRIES_CONFIG, "1")
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384")
    props.put(ProducerConfig.LINGER_MS_CONFIG, "1")
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432")
    props
  }

  val producer = new KafkaProducer[Array[Byte], Array[Byte]](kafkaProducerConfig)

  override def receive: Receive = {
    case Kafka.SpanMessage(message) =>
      producer.send(new ProducerRecord[Array[Byte], Array[Byte]](topic, message))
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    println("**** shutting down ...")
    producer.close()
    println("**** shutdown")
  }
}