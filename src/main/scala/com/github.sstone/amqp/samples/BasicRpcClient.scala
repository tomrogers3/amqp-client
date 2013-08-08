package com.github.sstone.amqp.samples

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.github.sstone.amqp.{RpcClient, RabbitMQConnection}
import com.github.sstone.amqp.RpcClient.Request
import com.github.sstone.amqp.Amqp.Publish


/**
 * start with mvn exec:java -Dexec.mainClass=com.github.sstone.amqp.samples.BasicRpcClient -Dexec.classpathScope="test"
 */
object BasicRpcClient extends App {
  import ExecutionContext.Implicits.global

  implicit val system = ActorSystem("mySystem")
  implicit val timeout: Timeout = 1 second
  // create an AMQP connection
  val conn = new RabbitMQConnection(host = "localhost", name = "Connection")

  val client = conn.createRpcClient()

  // send 1 request every second
  while(true) {
    (client ? Request(Publish("amq.direct", "my_key", "test".getBytes("UTF-8")))).mapTo[RpcClient.Response].map(response => {
      // we expect 1 delivery
      val delivery = response.deliveries.head
      println("reponse : " + new String(delivery.body))
    })
    Thread.sleep(1000)
  }
}
