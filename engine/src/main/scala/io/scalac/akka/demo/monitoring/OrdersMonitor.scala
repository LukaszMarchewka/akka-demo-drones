package io.scalac.akka.demo.monitoring

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}
import io.scalac.akka.demo.monitoring.OrdersMonitor._
import io.scalac.akka.demo.monitoring.mailbox.QueryPrioritizedMailbox
import io.scalac.akka.demo.order.Order
import io.scalac.akka.demo.types.Geolocation

/**
  * Monitors all orders.
  */
class OrdersMonitor extends Actor with ActorLogging {

	/**
	  * All orders (orderId -> data)
	  */
	var orders: Map[String, OrderData] = Map.empty

	override def preStart(): Unit = {
		context.system.eventStream.subscribe(self, classOf[Order.Event.OrderEvent])
	}

	override def receive: Receive = {
		case Order.Event.OrderCreated(orderId, where) =>
			orders = orders.updated(orderId, OrderData.idle(orderId, where))

		case Order.Event.OrderAssigned(orderId, where, droneId) =>
			orders = orders.updated(orderId, OrderData.assigned(orderId, where, droneId))

		case Order.Event.OrderAborted(orderId, where) =>
			orders = orders.updated(orderId, OrderData.idle(orderId, where))

		case Order.Event.OrderCompleted(orderId) =>
			orders = orders - orderId

		case Message.GetSnapshot =>
			sender() ! Response.Snapshot(orders.values.toList)

	}
}

object OrdersMonitor {
	def props = Props(new OrdersMonitor)

	object Message {

		/**
		  * Get a snapshot with all orders.
		  * Dispatches [[Response.Snapshot]] message to a sender.
		  */
		case object GetSnapshot extends QueryPrioritizedMailbox.QueryMessage

	}

	object Response {

		/**
		  * Response message with all orders.
		  *
		  * @param orders list of orders.
		  */
		case class Snapshot(orders: List[OrderData])

	}

	/**
	  * Status of an order.
	  */
	sealed trait OrderStatus

	object OrderStatuses {

		case object Idle extends OrderStatus

		case object Processing extends OrderStatus

	}

	case class OrderData(orderId: String,
	                     where: Geolocation,
	                     updatedAt: LocalDateTime,
	                     status: OrderStatus,
	                     droneId: Option[String])

	object OrderData {
		def idle(orderId: String, where: Geolocation) =
			OrderData(orderId, where, LocalDateTime.now(), OrderStatuses.Idle, None)

		def assigned(orderId: String, where: Geolocation, droneId: String) =
			OrderData(orderId, where, LocalDateTime.now(), OrderStatuses.Processing, Some(droneId))
	}

}