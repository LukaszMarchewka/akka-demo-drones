package io.scalac.akka.demo.drone

import akka.actor.{Actor, ActorLogging, Props}
import io.scalac.akka.demo.drone.Drones._
import io.scalac.akka.demo.types.Geolocation

/**
  * Manages all instances of drones.
  *
  * @param hqLoc location of the headquarter.
  */
class Drones(hqLoc: Geolocation) extends Actor with ActorLogging {

	/**
	  * Id of the next drone which will be created.
	  */
	var nextDroneId = 0

	override def receive: Receive = {
		case Message.Create =>
			val droneId = nextDroneId.toString
			nextDroneId += 1

			if (context.child(droneId).isEmpty) {
				val drone = context.actorOf(Drone.props(droneId, hqLoc), droneId)
				drone ! Drone.Message.Fly(Geolocation.randomLocation(hqLoc))
			}

		case Message.GetStatuses =>
			context.children.foreach(drone => drone.tell(Drone.Message.GetStatus, sender()))
	}
}

object Drones {
	def props(hqLoc: Geolocation): Props = Props(new Drones(hqLoc))

	object Message {

		/**
		  * Create a drone.
		  */
		case object Create

		/**
		  * Get statuses of all drones.
		  * A sender will receive [[Drone.Response.CurrentStatus]] message from each drone.
		  */
		case object GetStatuses
	}

}