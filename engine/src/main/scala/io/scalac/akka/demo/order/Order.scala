package io.scalac.akka.demo.order

import akka.actor.{ActorLogging, ActorRef, FSM, Props}
import io.scalac.akka.demo.drone.Drone
import io.scalac.akka.demo.monitoring.DronesRadar
import io.scalac.akka.demo.order.Order._
import io.scalac.akka.demo.types.Geolocation

import scala.concurrent.ExecutionContext

/**
  * Representation of an order.
  *
  * @param orderId     id of the order.
  * @param where       location of the order.
  * @param dronesRadar reference to [[io.scalac.akka.demo.monitoring.DronesRadar]]
  */
class Order(orderId: String, where: Geolocation, dronesRadar: ActorRef)
	extends FSM[Fsm.State, Fsm.Data] with ActorLogging {

	implicit val ex: ExecutionContext = context.dispatcher

	startWith(Fsm.SearchingCandidate, Fsm.EmptyData)

	override def preStart(): Unit = {
		log.info("[Order {}] Created an order for a location '{}'", orderId, where)
		dronesRadar ! DronesRadar.Message.GetTheNearestAvailable(where)
	}

	when(Fsm.SearchingCandidate) {
		case Event(DronesRadar.Response.MatchingDrone(droneId, drone), _) =>
			log.debug("[Order {}] Starting a negotiation process with a drone '{}'", orderId, droneId)
			drone ! Drone.Message.ProcessOrder(self, orderId, where)
			goto(Fsm.NegotiatingDeal) using Fsm.DroneData(drone, droneId)
	}

	when(Fsm.NegotiatingDeal) {
		case Event(Drone.Response.OrderRejected(`orderId`), Fsm.DroneData(_, droneId)) =>
			log.debug("[Order {}] Order has been rejected be the drone '{}'", orderId, droneId)
			dronesRadar ! DronesRadar.Message.GetTheNearestAvailable(where)
			goto(Fsm.SearchingCandidate) using Fsm.EmptyData

		case Event(Drone.Response.OrderAccepted(`orderId`), Fsm.DroneData(_, droneId)) =>
			log.debug("[Order {}] Order has been accepted be the drone '{}'", orderId, droneId)
			goto(Fsm.InProgress)
	}

	when(Fsm.InProgress) {
		case Event(Drone.Response.OrderAborted(`orderId`), Fsm.DroneData(_, droneId)) =>
			log.debug("[Order {}] Order has been aborted be the drone '{}'", orderId, droneId)
			dronesRadar ! DronesRadar.Message.GetTheNearestAvailable(where)
			goto(Fsm.SearchingCandidate) using Fsm.EmptyData

		case Event(Drone.Response.OrderCompleted(`orderId`), Fsm.DroneData(_, droneId)) =>
			log.debug("[Order {}] Order has been completed be the drone '{}'", orderId, droneId)
			stop
	}
}

object Order {

	def props(id: String, to: Geolocation, dronesRadar: ActorRef): Props = Props(new Order(id, to, dronesRadar))

	private[Order] object Fsm {

		sealed trait State

		case object SearchingCandidate extends State

		case object NegotiatingDeal extends State

		case object InProgress extends State

		sealed trait Data

		case object EmptyData extends Data

		case class DroneData(drone: ActorRef, droneId: String) extends Data

	}

}