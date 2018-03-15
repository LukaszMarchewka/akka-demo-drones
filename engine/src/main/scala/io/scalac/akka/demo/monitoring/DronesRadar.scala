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
	var snapshot: Map[String, DroneData] = Map.empty

	context.setReceiveTimeout(100.millis)

	override def receive: Receive = {
		case ReceiveTimeout =>
			drones ! Drones.Message.GetStatuses

		case Drone.Response.CurrentStatus(drone, droneId, location, orderId) =>
			snapshot = snapshot.updated(droneId, DroneData(droneId, drone, location, orderId, LocalDateTime.now))

		case Message.GetSnapshot =>
			sender() ! Response.Snapshot(snapshot.values.toList)

		case Message.GetTheNearestAvailable(requestedLoc: Geolocation) =>
			val available = snapshot.values.filter(data => data.orderId.isEmpty && data.age() <= 1)
			val theNearest = available.foldLeft(Option.empty[DroneData]) { (theNearest, next) =>
				theNearest match {
					case None =>
						Some(next)
					case Some(n) if Geolocation.distance(next.loc, requestedLoc) < Geolocation.distance(n.loc, requestedLoc) =>
						Some(next)
					case n => n
				}
			}
			theNearest.foreach(data => sender() ! Response.MatchingDrone(data.droneId, data.drone))
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

		/**
		  * Get a drone which is an available (doesn't do any job) and is the nearest of a some location.
		  * Dispatches [[Response.MatchingDrone]] message to a sender on successful search.
		  *
		  * @param from searching location.
		  */
		case class GetTheNearestAvailable(from: Geolocation)

	}

	object Response {

		/**
		  * Response message with all drones.
		  *
		  * @param drones list of drones.
		  */
		case class Snapshot(drones: List[DroneData])

		/**
		  * Response message with a matching drone.
		  *
		  * @param droneId id of the drone.
		  * @param drone   actor of the drone.
		  */
		case class MatchingDrone(droneId: String, drone: ActorRef)

	}

	case class DroneData(droneId: String, drone: ActorRef, loc: Geolocation, orderId: Option[String], seen: LocalDateTime) {
		def age(now: LocalDateTime = LocalDateTime.now): Int = seen.until(now, ChronoUnit.SECONDS).toInt
	}

}