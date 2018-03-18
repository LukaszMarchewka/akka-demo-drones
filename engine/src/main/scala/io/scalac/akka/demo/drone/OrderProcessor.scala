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

	startWith(Fsm.Waiting, Fsm.EmptyData)

	when(Fsm.Waiting) {
		case Event(Drone.Message.ProcessOrder(order, orderId, where), _) =>
			log.info("[Drone {}] Processing of an order '{}' from location {}", droneId, orderId, where)
			goto(Fsm.Processing) using Fsm.OrderData(order, orderId, where)

		case Event(Message.GetProcessingOrder, _) =>
			sender() ! Response.ProcessingOrder(None)
			stay
	}

	when(Fsm.Processing) {
		case Event(Drone.Message.ProcessOrder(_, newOrderId, _), Fsm.OrderData(_, currentOrderId, _)) =>
			log.info("[Drone {}] New order with the id '{}' can't be processed because it is processing order '{}'", droneId, newOrderId, currentOrderId)
			sender() ! Drone.Response.OrderRejected(newOrderId)
			stay

		case Event(Drone.Response.TargetDestinationReached(_, `droneId`, reachedDestination), Fsm.OrderData(order, orderId, where)) if reachedDestination == where =>
			order ! Drone.Response.OrderCompleted(orderId)
			goto(Fsm.Waiting) using Fsm.EmptyData

		case Event(Drone.Response.FlyAborted(_, `droneId`, reachedDestination), Fsm.OrderData(order, orderId, where)) if reachedDestination == where =>
			order ! Drone.Response.OrderAborted(orderId)
			goto(Fsm.Waiting) using Fsm.EmptyData

		case Event(Message.GetProcessingOrder, Fsm.OrderData(_, orderId, _)) =>
			sender() ! Response.ProcessingOrder(Some(orderId))
			stay
	}

	onTransition {
		case Fsm.Waiting -> Fsm.Processing =>
			nextStateData match {
				case Fsm.OrderData(order, orderId, where) =>
					drone ! Drone.Message.Fly(where)
					order ! Drone.Response.OrderAccepted(orderId)
				case Fsm.EmptyData => //nothing to do
			}
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

		case object EmptyData extends Data

		case class OrderData(order: ActorRef, orderId: String, where: Geolocation) extends Data

	}

}