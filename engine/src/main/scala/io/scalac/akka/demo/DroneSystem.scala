package io.scalac.akka.demo

import akka.actor.ActorSystem

trait DroneSystem {
	val system: ActorSystem = ActorSystem("drones")
}