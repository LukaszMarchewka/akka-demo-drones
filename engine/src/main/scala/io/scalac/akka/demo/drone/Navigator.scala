package io.scalac.akka.demo.drone

import akka.actor.{ActorLogging, ActorRef, Cancellable, FSM, Props}
import io.scalac.akka.demo.drone.Navigator._
import io.scalac.akka.demo.types.Geolocation

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Navigate a drone from one location to another one.
  *
  * @param drone   reference to a drone actor.
  * @param droneId id of the drone.
  * @param hqLoc   location of the headquarter, it is also initials location.
  */
private[drone] class Navigator(drone: ActorRef, droneId: String, hqLoc: Geolocation) extends FSM[Fsm.State, Fsm.Data] with ActorLogging {

	implicit val ex: ExecutionContext = context.dispatcher

	var current: Geolocation = hqLoc

	startWith(Fsm.Resting, Fsm.RestingData)

	when(Fsm.Resting) {
		case Event(Drone.Message.Fly(to), _) =>
			log.info("[Drone {}] Started a navigation to {}", droneId, to)
			val scheduler = context.system.scheduler.schedule(200.millis, 200.millis, self, Tick)
			goto(Fsm.Moving) using Fsm.MovingData(sender(), to, scheduler)

		case Event(Tick, _) =>
			stay
	}

	when(Fsm.Moving) {
		case Event(Drone.Message.Fly(to), Fsm.MovingData(previousRequester, previousTo, scheduler)) =>
			previousRequester ! Drone.Response.FlyAborted(drone, droneId, previousTo)
			log.info("[Drone {}] Changed a target location to {}", droneId, to)
			stay using Fsm.MovingData(sender(), to, scheduler)

		case Event(Tick, Fsm.MovingData(requester, to, scheduler)) =>
			val delta: Geolocation = Geolocation.delta(current, to, speed)
			current = Geolocation(current.lat + delta.lat, current.long + delta.long)

			if (isTargetReached(delta, to)) {
				current = to
				requester ! Drone.Response.TargetDestinationReached(drone, droneId, to)
				scheduler.cancel()
				log.info("[Drone {}] Reached the target location {}", droneId, to)
				goto(Fsm.Resting) using Fsm.RestingData
			} else {
				stay
			}
	}

	whenUnhandled {
		case Event(Drone.Message.GetLocation, _) =>
			sender() ! Drone.Response.CurrentLocation(drone, droneId, current)
			stay
	}

	def isTargetReached(delta: Geolocation, to: Geolocation): Boolean = {
		Math.abs(current.lat - to.lat) < Math.abs(delta.lat) && Math.abs(current.long - to.long) < Math.abs(delta.long)
	}
}

private[drone] object Navigator {
	def props(drone: ActorRef, droneId: String, hqLoc: Geolocation): Props = Props(new Navigator(drone, droneId, hqLoc))

	private val speed: Double = 0.001

	private[Navigator] case object Tick

	private[Navigator] object Fsm {

		sealed trait State

		case object Resting extends State

		case object Moving extends State

		sealed trait Data

		case object RestingData extends Data

		case class MovingData(requester: ActorRef, to: Geolocation, scheduler: Cancellable) extends Data

	}

}