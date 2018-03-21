package io.scalac.akka.demo.order

import akka.actor.{ActorLogging, ActorRef, FSM, Props}
import io.scalac.akka.demo.config.Config.droneNavigationInterval
import io.scalac.akka.demo.drone.Drone
import io.scalac.akka.demo.monitoring.DronesRadar
import io.scalac.akka.demo.order.Order._
import io.scalac.akka.demo.types.Geolocation
import io.scalac.akka.demo.types.Geolocation.{Distance, Speed}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

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

	startWith(Fsm.Idle, Fsm.EmptyData)

	override def preStart(): Unit = {
		log.info("[Order {}] Created an order for a location '{}'", orderId, where)
		context.system.eventStream.publish(Order.Event.OrderCreated(orderId, where))
		context.system.scheduler.scheduleOnce(Random.nextInt(2000).millis, self, StateTimeout)
	}

	when(Fsm.Idle, Random.nextInt(2000).millis + 10.seconds) {
		case Event(StateTimeout, _) =>
			goto(Fsm.WaitingForCandidate) using Fsm.EmptyData

		case Event(DronesRadar.Response.NearestDrone(droneId, drone, distance, speed), _) =>
			log.debug("[Order {}] The nearest drone is '{}'", orderId, droneId)
			goto(Fsm.NegotiatingDeal) using Fsm.DroneData(drone, droneId, distance, speed)

		case Event(Drone.Response.OrderAccepted(`orderId`), Fsm.DroneData(_, droneId, distance, speed)) =>
			log.debug("[Order {}] The order has been accepted be the drone '{}'", orderId, droneId)
			goto(Fsm.InProgress) forMax ((distance / speed) * 1.5 * droneNavigationInterval)
	}

	when(Fsm.WaitingForCandidate, 1.second) {
		case Event(DronesRadar.Response.NearestDrone(droneId, drone, distance, speed), _) =>
			log.debug("[Order {}] The nearest drone is '{}'", orderId, droneId)
			goto(Fsm.NegotiatingDeal) using Fsm.DroneData(drone, droneId, distance, speed)

		case Event(StateTimeout, _) =>
			goto(Fsm.Idle) using Fsm.EmptyData
	}

	when(Fsm.NegotiatingDeal, 1.second) {
		case Event(Drone.Response.OrderAccepted(`orderId`), Fsm.DroneData(_, droneId, distance, speed)) =>
			log.debug("[Order {}] The order has been accepted be the drone '{}'", orderId, droneId)
			goto(Fsm.InProgress) forMax ((distance / speed) * 1.5 * droneNavigationInterval)

		case Event(Drone.Response.OrderRejected(`orderId`), Fsm.DroneData(_, droneId, _, _)) =>
			log.debug("[Order {}] The order has been rejected be the drone '{}'", orderId, droneId)
			goto(Fsm.Idle) using Fsm.EmptyData

		case Event(StateTimeout, _) =>
			goto(Fsm.Idle) using Fsm.EmptyData
	}

	when(Fsm.InProgress) {
		case Event(Drone.Response.OrderCompleted(`orderId`), Fsm.DroneData(_, droneId, _, _)) =>
			log.debug("[Order {}] The order has been completed be the drone '{}'", orderId, droneId)
			stop

		case Event(Drone.Response.OrderAborted(`orderId`), Fsm.DroneData(_, droneId, _, _)) =>
			log.debug("[Order {}] The order has been aborted be the drone '{}'", orderId, droneId)
			goto(Fsm.Idle) using Fsm.EmptyData forMax 1.second

		case Event(StateTimeout, _) =>
			goto(Fsm.Idle) using Fsm.EmptyData forMax 1.second
	}

	onTransition {
		case _ -> Fsm.WaitingForCandidate =>
			dronesRadar ! DronesRadar.Message.GetTheNearestAvailable(where)

		case _ -> Fsm.NegotiatingDeal =>
			nextStateData match {
				case Fsm.DroneData(drone, _, _, _) =>
					drone ! Drone.Message.ProcessOrder(self, orderId, where)
				case Fsm.EmptyData => //nothing to do
			}

		case _ -> Fsm.InProgress =>
			nextStateData match {
				case Fsm.DroneData(_, droneId, _, _) =>
					context.system.eventStream.publish(Order.Event.OrderAssigned(orderId, where, droneId))
				case Fsm.EmptyData => //nothing to do
			}

		case Fsm.InProgress -> Fsm.Idle =>
			context.system.eventStream.publish(Order.Event.OrderAborted(orderId, where))
	}

	override def postStop(): Unit = {
		context.system.eventStream.publish(Order.Event.OrderCompleted(orderId))
	}
}

object Order {

	def props(id: String, to: Geolocation, dronesRadar: ActorRef): Props = Props(new Order(id, to, dronesRadar))

	private[Order] object Fsm {

		sealed trait State

		case object Idle extends State

		case object WaitingForCandidate extends State

		case object NegotiatingDeal extends State

		case object InProgress extends State

		sealed trait Data

		case object EmptyData extends Data

		case class DroneData(drone: ActorRef, droneId: String, distance: Distance, speed: Speed) extends Data

	}

	object Event {

		sealed trait OrderEvent {
			val orderId: String
		}

		case class OrderCreated(orderId: String, where: Geolocation) extends OrderEvent

		case class OrderAssigned(orderId: String, where: Geolocation, droneId: String) extends OrderEvent

		case class OrderAborted(orderId: String, where: Geolocation) extends OrderEvent

		case class OrderCompleted(orderId: String) extends OrderEvent

	}

}