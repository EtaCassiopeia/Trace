package com.github.etacassiopeia.trace.kafka

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import akka.io.IO
import akka.routing.SmallestMailboxPool

/**
  * <h1>Kafka</h1>
  * The Kafka 
  *
  * @author Mohsen Zainalpour
  * @version 1.0
  * @since 5/04/16 
  */
class Kafka(system: ActorSystem) extends IO.Extension {

  private val config = system.settings.config.getConfig("kafka.producer")

  private val managerDispatcher = config.getString("manager-dispatcher")
  private val routeeProps = Props(classOf[KafkaProducerManager], config).withDispatcher(managerDispatcher)
  private val poolSize = config.getInt("pool-size")
  private val managerProps = SmallestMailboxPool(nrOfInstances = poolSize, routerDispatcher = managerDispatcher).props(routeeProps)
  private val managerActor = system.actorOf(managerProps)

  override def manager: ActorRef = managerActor
}


object Kafka extends ExtensionId[Kafka] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): Kafka = new Kafka(system)

  override def lookup(): ExtensionId[_ <: Extension] = Kafka

  case class SpanMessage[K, V](topic: String, key: Array[Byte], payload: Array[Byte])

}