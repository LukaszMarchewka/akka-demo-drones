package io.scalac.akka.demo.drone

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import io.scalac.akka.demo.types.Geolocation

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Fetches status of a drone.
  * The actor will be created on each [[Drone.Message.GetStatus]] message.
  * TODO we can use FSM and transform for it.
  *
  * @param drone     actor of the drone.
  * @param droneId   id of the drone.
  * @param requester actor which has asked about status.
  * @param navigator actor of a navigator of the drone.
  */
private[drone] class GetStatus(drone: ActorRef, droneId: String, requester: ActorRef, navigator: ActorRef) extends Actor with ActorLogging {

	implicit val ex: ExecutionContext = context.dispatcher
	val timeout: Cancellable = context.system.scheduler.scheduleOnce(100.millis, self, PoisonPill)

	/**
	  * Current location.
	  */
	var optionalLocation: Option[Geolocation] = None

	override def preStart(): Unit = {
		navigator ! Drone.Message.GetLocation
	}

	override def receive: Receive = {
		case Drone.Response.CurrentLocation(_, `droneId`, currentLocation) =>
			optionalLocation = Some(currentLocation)
			maybeFinish()
	}

	def maybeFinish(): Unit = {
		for {
			location <- optionalLocation
		} yield {
			requester ! Drone.Response.CurrentStatus(drone, droneId, location)
			timeout.cancel()
			context.stop(self)
		}
	}
}

private[drone] object GetStatus {
	def props(drone: ActorRef, droneId: String, requester: ActorRef, navigator: ActorRef): Props =
		Props(new GetStatus(drone, droneId, requester, navigator))
}
