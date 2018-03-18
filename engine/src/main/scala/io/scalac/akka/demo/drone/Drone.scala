package io.scalac.akka.demo.drone

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scalac.akka.demo.drone.Drone._
import io.scalac.akka.demo.types.Geolocation
import io.scalac.akka.demo.types.Geolocation.Speed

import scala.util.Random

/**
  * Representation of a drone.
  *
  * @param droneId id of the drone.
  * @param hqLoc   location of the headquarter.
  */
class Drone(droneId: String, hqLoc: Geolocation) extends Actor with ActorLogging {

	val stability: Double = Random.nextDouble() / 4

	/**
	  * Actor for navigation of the drone.
	  */
	val navigator: ActorRef = context.actorOf(Navigator.props(self, droneId, hqLoc), "navigator")

	/**
	  * Actor for processing orders.
	  */
	val orderProcessor: ActorRef = context.actorOf(OrderProcessor.props(self, droneId), "order")

	override def receive: Receive = {
		case msg: Message.Fly =>
			navigator forward msg
		case msg: Message.ProcessOrder =>
			orderProcessor forward msg
		case Message.GetStatus =>
			if (Random.nextDouble() <= stability) {
				val uuid = UUID.randomUUID().toString
				context.actorOf(GetStatus.props(self, droneId, sender(), navigator, orderProcessor), s"status-$uuid")
			}
	}
}

object Drone {
	def props(id: String, hqLoc: Geolocation): Props = Props(new Drone(id, hqLoc))

	object Message {

		/**
		  * Flies to a target destination.
		  * Dispatches a [[Response.TargetDestinationReached]] message to the sender on reached the target destination.
		  * Dispatches a [[Response.FlyAborted]] message to the sender on aborted fly operation to the target destination.
		  *
		  * @param to location of the target destination.
		  */
		case class Fly(to: Geolocation)

		/**
		  * Processes an order.
		  * Following two messages will be dispatches as a direct response on [[Message.ProcessOrder]]:
		  * Dispatcher [[Response.OrderRejected]] on a rejected order.
		  * Dispatcher [[Response.OrderAccepted]] on an accepted order by a drone.
		  * Following two messages will be dispatched during a processing of the order.
		  * Dispatcher [[Response.OrderCompleted]] on a completed order.
		  * Dispatcher [[Response.OrderAborted]] on an aborted order.
		  *
		  * @param order   reference to [[io.scalac.akka.demo.order.Order]].
		  * @param orderId id of the order.
		  * @param where   location of the order.
		  */
		case class ProcessOrder(order: ActorRef, orderId: String, where: Geolocation)

		/**
		  * Gets a status of a drone.
		  * Dispatches a [[Response.CurrentStatus]] message to the sender with the current status.
		  */
		case object GetStatus

	}

	object Response {

		/**
		  * Response massage dispatched to the sender of [[Message.Fly]] after reaching a target location.
		  *
		  * @param drone   actor of the drone.
		  * @param droneId id of the drone.
		  * @param loc     target location.
		  */
		case class TargetDestinationReached(drone: ActorRef, droneId: String, loc: Geolocation)

		/**
		  * Response message dispatched to the sender of [[Message.Fly]] on aborted fly to a target destination.
		  *
		  * @param drone   actor of the drone.
		  * @param droneId id of the drone.
		  * @param loc     previous target location which has been aborted.
		  */
		case class FlyAborted(drone: ActorRef, droneId: String, loc: Geolocation)

		/**
		  * Response message with a current status of the drone.
		  *
		  * @param drone           actor of the drone.
		  * @param droneId         id of the drone.
		  * @param currentLocation current location of the drone.
		  * @param targetLocation  optional target location (only for flying the drone).
		  * @param speed           speed of the drone.
		  * @param currentOrderId  id of current order, None - not processing an order.
		  */
		case class CurrentStatus(drone: ActorRef,
		                         droneId: String,
		                         currentLocation: Geolocation,
		                         targetLocation: Option[Geolocation],
		                         speed: Speed,
		                         currentOrderId: Option[String])

		/**
		  * Order hes been rejected and wasn't assigned to a drone.
		  *
		  * @param orderId id of the order.
		  */
		case class OrderRejected(orderId: String)

		/**
		  * Order hes been accepted ba a drone and will be processing now.
		  *
		  * @param orderId id of the order.
		  */
		case class OrderAccepted(orderId: String)

		/**
		  * Order hes been completed.
		  *
		  * @param orderId id of the order.
		  */
		case class OrderCompleted(orderId: String)

		/**
		  * Order hes been aborted during processing.
		  *
		  * @param orderId id of the order.
		  */
		case class OrderAborted(orderId: String)

	}

}