package io.scalac.akka.demo.main

import io.scalac.akka.demo.DroneSystem
import io.scalac.akka.demo.api.WebServer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object Main {

	def main(args: Array[String]): Unit = {
		val demo = new MainServer()

		try StdIn.readLine()
		finally demo.stop()
	}
}

class MainServer extends DroneSystem {
	val demo = new WebServer(drones, dronesRadar, orders, ordersMonitor)(system)

	def stop(): Unit = {
		demo.stop().onComplete(_ => system.terminate())
	}
}