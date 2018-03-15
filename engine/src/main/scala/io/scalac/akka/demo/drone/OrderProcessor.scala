package io.scalac.akka.demo.drone

import akka.actor.{ActorLogging, ActorRef, FSM, Props}
import io.scalac.akka.demo.drone.OrderProcessor._
import io.scalac.akka.demo.types.Geolocation

import scala.concurrent.ExecutionContext

/**
  * Processes incoming orders.
  *
  * @param drone   reference to a drone actor.
  * @param droneId id of the drone.
  */
private[drone] class OrderProcessor(drone: ActorRef, droneId: String) extends FSM[Fsm.State, Fsm.Data] with ActorLogging {

	implicit val ex: ExecutionContext = context.dispatcher

	startWith(Fsm.Waiting, Fsm.WaitingData)

	when(Fsm.Waiting) {
		case Event(Drone.Message.ProcessOrder(order, orderId, where), _) =>
			log.info("[Drone {}] Processing of an order '{}' from location {}", droneId, orderId, where)
			drone ! Drone.Message.Fly(where)
			sender() ! Drone.Response.OrderAccepted(orderId)
			goto(Fsm.Processing) using Fsm.ProcessingData(order, orderId, where)

		case Event(Message.GetProcessingOrder, _) =>
			sender() ! Response.ProcessingOrder(None)
			stay
	}

	when(Fsm.Processing) {
		case Event(Drone.Message.ProcessOrder(_, newOrderId, _), Fsm.ProcessingData(_, currentOrderId, _)) =>
			log.info("[Drone {}] New order with the id '{}' can't be processed because it is processing order '{}'", droneId, newOrderId, currentOrderId)
			sender() ! Drone.Response.OrderRejected(newOrderId)
			stay

		case Event(Drone.Response.TargetDestinationReached(_, `droneId`, reachedDestination), Fsm.ProcessingData(order, orderId, where)) if reachedDestination == where =>
			order ! Drone.Response.OrderCompleted(orderId)
			goto(Fsm.Waiting) using Fsm.WaitingData

		case Event(Drone.Response.FlyAborted(_, `droneId`, reachedDestination), Fsm.ProcessingData(order, orderId, where)) if reachedDestination == where =>
			order ! Drone.Response.OrderAborted(orderId)
			goto(Fsm.Waiting) using Fsm.WaitingData

		case Event(Message.GetProcessingOrder, Fsm.ProcessingData(_, orderId, _)) =>
			sender() ! Response.ProcessingOrder(Some(orderId))
			stay
	}

}

private[drone] object OrderProcessor {
	def props(drone: ActorRef, droneId: String): Props = Props(new OrderProcessor(drone, droneId))

	object Message {

		/**
		  * Gets processing an order.
		  */
		case object GetProcessingOrder

	}

	object Response {

		/**
		  * Response message with currently processing an order.
		  *
		  * @param orderId id of the order.
		  */
		case class ProcessingOrder(orderId: Option[String])

	}

	private[OrderProcessor] object Fsm {

		sealed trait State

		case object Waiting extends State

		case object Processing extends State

		sealed trait Data

		case object WaitingData extends Data

		case class ProcessingData(order: ActorRef, orderId: String, where: Geolocation) extends Data

	}

}