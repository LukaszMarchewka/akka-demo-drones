package io.scalac.akka.demo.drone

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import io.scalac.akka.demo.drone.GetStatus.LocationData
import io.scalac.akka.demo.types.Geolocation
import io.scalac.akka.demo.types.Geolocation.Speed

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Fetches status of a drone.
  * The actor will be created on each [[Drone.Message.GetStatus]] message.
  * TODO we can use FSM and transform for it.
  *
  * @param drone          actor of the drone.
  * @param droneId        id of the drone.
  * @param requester      actor which has asked about status.
  * @param navigator      actor of a navigator of the drone.
  * @param orderProcessor actor processing orders.
  */
private[drone] class GetStatus(drone: ActorRef,
                               droneId: String,
                               requester: ActorRef,
                               navigator: ActorRef,
                               orderProcessor: ActorRef)
	extends Actor with ActorLogging {

	implicit val ex: ExecutionContext = context.dispatcher
	val timeout: Cancellable = context.system.scheduler.scheduleOnce(100.millis, self, PoisonPill)

	/**
	  * Current location.
	  */
	var optionalLocation: Option[LocationData] = None
	/**
	  * Current order.
	  */
	var optionalOrder: Option[Either[Unit, String]] = None

	override def preStart(): Unit = {
		navigator ! Navigator.Message.GetLocation
		orderProcessor ! OrderProcessor.Message.GetProcessingOrder
	}

	override def receive: Receive = {
		case Navigator.Response.CurrentLocation(_, `droneId`, currentLocation, targetLocation, speed) =>
			optionalLocation = Some(LocationData(currentLocation, targetLocation, speed))
			maybeFinish()

		case OrderProcessor.Response.ProcessingOrder(Some(orderId)) =>
			optionalOrder = Some(Right(orderId))
			maybeFinish()

		case OrderProcessor.Response.ProcessingOrder(None) =>
			optionalOrder = Some(Left(Unit))
			maybeFinish()
	}

	def maybeFinish(): Unit = {
		for {
			location <- optionalLocation
			order <- optionalOrder
		} yield {
			requester ! Drone.Response.CurrentStatus(drone, droneId, location.current, location.target, location.speed, order.toOption)
			timeout.cancel()
			context.stop(self)
		}
	}
}

private[drone] object GetStatus {
	def props(drone: ActorRef, droneId: String, requester: ActorRef, navigator: ActorRef, orderProcessor: ActorRef): Props =
		Props(new GetStatus(drone, droneId, requester, navigator, orderProcessor))

	private[GetStatus] case class LocationData(current: Geolocation, target: Option[Geolocation], speed: Speed)

}
