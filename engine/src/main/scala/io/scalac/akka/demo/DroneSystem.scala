package io.scalac.akka.demo

import akka.actor.{ActorRef, ActorSystem}
import io.scalac.akka.demo.drone.Drones
import io.scalac.akka.demo.monitoring.{DronesRadar, OrdersMonitor}
import io.scalac.akka.demo.order.Orders
import io.scalac.akka.demo.types.Geolocation

trait DroneSystem {
	val hqLoc = Geolocation(53.117046, 23.146447)

	val system: ActorSystem = ActorSystem("drones")

	val drones: ActorRef = system.actorOf(Drones.props(hqLoc), "drones")

	val dronesRadar: ActorRef = system.actorOf(DronesRadar.props(drones).withMailbox("prio-mailbox"), "dronesRadar")

	val ordersMonitor: ActorRef = system.actorOf(OrdersMonitor.props.withMailbox("prio-mailbox"), "ordersMonitor")

	val orders: ActorRef = system.actorOf(Orders.props(dronesRadar), "orders")

}