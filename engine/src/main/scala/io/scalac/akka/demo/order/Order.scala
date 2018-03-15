package io.scalac.akka.demo.order

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scalac.akka.demo.types.Geolocation

import scala.concurrent.ExecutionContext

/**
  * Representation of an order.
  *
  * @param orderId     id of the order.
  * @param where       location of the order.
  * @param dronesRadar reference to [[io.scalac.akka.demo.monitoring.DronesRadar]]
  */
class Order(orderId: String, where: Geolocation, dronesRadar: ActorRef) extends Actor with ActorLogging {

	implicit val ex: ExecutionContext = context.dispatcher

	override def preStart(): Unit = {
		log.info("[Order {}] Created an order for a location '{}'", orderId, where)
	}

	override def receive: Receive = {
		case _ => ???
	}
}

object Order {

	def props(id: String, to: Geolocation, dronesRadar: ActorRef): Props = Props(new Order(id, to, dronesRadar))

}