package io.scalac.akka.demo.monitoring

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import io.scalac.akka.demo.drone.{Drone, Drones}
import io.scalac.akka.demo.monitoring.DronesRadar._
import io.scalac.akka.demo.types.Geolocation

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Radar which tracks of all drones.
  *
  * @param drones reference to [[io.scalac.akka.demo.drone.Drones]]
  */
class DronesRadar(drones: ActorRef) extends Actor with ActorLogging {

	implicit val ex: ExecutionContext = context.dispatcher

	/**
	  * Map with all drones. (droneId -> data)
	  */
	var cache: Map[String, DroneData] = Map.empty

	context.setReceiveTimeout(100.millis)

	override def receive: Receive = {
		case ReceiveTimeout =>
			drones ! Drones.Message.GetStatuses

		case Drone.Response.CurrentStatus(_, droneId, location) =>
			cache = cache.updated(droneId, DroneData(droneId, location, LocalDateTime.now))

		case Message.GetSnapshot =>
			sender() ! Response.Snapshot(cache.values.toList)
	}
}

object DronesRadar {
	def props(drones: ActorRef): Props = Props(new DronesRadar(drones))

	object Message {

		/**
		  * Get a snapshot with all drones.
		  * Dispatches [[Response.Snapshot]] message to a sender.
		  */
		case object GetSnapshot

	}

	object Response {

		/**
		  * Response message with all drones.
		  *
		  * @param drones list of drones.
		  */
		case class Snapshot(drones: List[DroneData])

	}

	case class DroneData(droneId: String, loc: Geolocation, seen: LocalDateTime) {
		def age(now: LocalDateTime = LocalDateTime.now): Int = seen.until(now, ChronoUnit.SECONDS).toInt
	}

}