package io.scalac.akka.demo.order

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scalac.akka.demo.order.Orders._
import io.scalac.akka.demo.types.Geolocation

/**
  * Manage all orders requested by a client.
  *
  * @param dronesRadar reference to [[io.scalac.akka.demo.monitoring.DronesRadar]]
  */
class Orders(dronesRadar: ActorRef) extends Actor with ActorLogging {

	/**
	  * Id of the next order which will be created.
	  */
	var nextOrderId = 0

	override def receive: Receive = {
		case Message.CreateOrder(loc) =>
			val orderId = nextOrderId.toString
			nextOrderId += 1

			context.actorOf(Order.props(orderId, loc, dronesRadar), orderId)
	}
}

object Orders {

	def props(dronesRadar: ActorRef): Props = Props(new Orders(dronesRadar))

	object Message {

		/**
		  * Message to create an order.
		  *
		  * @param where location of the order.
		  */
		case class CreateOrder(where: Geolocation)

	}

}