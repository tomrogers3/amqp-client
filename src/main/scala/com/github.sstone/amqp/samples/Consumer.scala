package com.github.sstone.amqp.samples

import akka.actor.{Props, Actor, ActorSystem}
import com.github.sstone.amqp.{Consumer, Amqp, RabbitMQConnection}
import com.github.sstone.amqp.Amqp._
import com.github.sstone.amqp.Amqp.Ack
import com.github.sstone.amqp.Amqp.QueueParameters
import com.github.sstone.amqp.Amqp.Delivery

object Consumer extends App {
  implicit val system = ActorSystem("mySystem")

  // create an AMQP connection
  val conn = new RabbitMQConnection(host = "localhost", name = "Connection")

  // create an actor that will receive AMQP deliveries
  val listener = system.actorOf(Props(new Actor {
    def receive = {
      case Delivery(consumerTag, envelope, properties, body) => {
        println("got a message: " + new String(body))
        sender ! Ack(envelope.getDeliveryTag)
      }
    }
  }))

  // create a consumer that will route incoming AMQP messages to our listener

  // option 1: create the queue and binding, and then consume from the queue
  val consumer = conn.createChild(Props(new Consumer(listener = Some(listener))))
  // wait till everyone is actually connected to the broker
  Amqp.waitForConnection(system, consumer).await()

  val queueParams = QueueParameters("my_queue", passive = false, durable = false, exclusive = false, autodelete = true)
  consumer ! DeclareQueue(queueParams)
  consumer ! QueueBind(queue = "my_queue", exchange = "amq.direct", routing_key = "my_key")
  consumer ! AddQueue(queue = "my_queue")


  // run the Producer sample now and see what happens
  println("press enter...")
  System.in.read()
  system.shutdown()
}
