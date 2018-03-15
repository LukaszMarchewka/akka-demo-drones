package io.scalac.akka.demo.drone

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.scalac.akka.demo.drone.Drone._
import io.scalac.akka.demo.types.Geolocation

/**
  * Representation of a drone.
  *
  * @param droneId id of the drone.
  * @param hqLoc   location of the headquarter.
  */
class Drone(droneId: String, hqLoc: Geolocation) extends Actor with ActorLogging {

	/**
	  * Actor for navigation of the drone.
	  */
	val navigator: ActorRef = context.actorOf(Navigator.props(self, droneId, hqLoc), "navigator")

	override def receive: Receive = {
		case msg: Message.Fly =>
			navigator forward msg
		case msg: Message.GetLocation.type =>
			navigator forward msg
		case Message.GetStatus =>
			val uuid = UUID.randomUUID().toString
			context.actorOf(GetStatus.props(self, droneId, sender(), navigator), s"status-$uuid")
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
		  * Gets a current location of a drone.
		  * Dispatches a [[Response.CurrentLocation]] message to the sender with the current location.
		  */
		case object GetLocation

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
		  * Response message with a current location of the drone.
		  *
		  * @param drone           actor of the drone.
		  * @param droneId         id of the drone.
		  * @param currentLocation current location of the drone..
		  */
		case class CurrentLocation(drone: ActorRef, droneId: String, currentLocation: Geolocation)

		/**
		  * Response message with a current status of the drone.
		  *
		  * @param drone           actor of the drone.
		  * @param droneId         id of the drone.
		  * @param currentLocation current location of the drone..
		  */
		case class CurrentStatus(drone: ActorRef, droneId: String, currentLocation: Geolocation)

	}

}