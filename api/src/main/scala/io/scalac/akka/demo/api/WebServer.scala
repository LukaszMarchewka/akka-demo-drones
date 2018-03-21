package io.scalac.akka.demo.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.scalac.akka.demo.api.model._
import io.scalac.akka.demo.drone.Drones
import io.scalac.akka.demo.monitoring.{DronesRadar, OrdersMonitor}
import io.scalac.akka.demo.order.Orders
import io.scalac.akka.demo.types.Geolocation

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class WebServer(drones: ActorRef, dronesRadar: ActorRef, orders: ActorRef, ordersMonitor: ActorRef)
               (implicit val system: ActorSystem) extends Directives {
	private implicit val materializer: ActorMaterializer = ActorMaterializer()
	private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
	private implicit val timeout: Timeout = 200.millis

	private val route: Route = pathPrefix("api") {
		path("drones") {
			get {
				complete {
					(dronesRadar ? DronesRadar.Message.GetSnapshot)
						.mapTo[DronesRadar.Response.Snapshot]
						.map(_.drones.map { drone =>
							DroneApiDto(drone.droneId,
								GeolocationApiDto(drone.current),
								drone.target.map(GeolocationApiDto.apply),
								drone.orderId,
								drone.age())
						})
				}
			} ~ post {
				entity(as[CreateDronesApiDto]) { request =>
					(1 to request.number).foreach(_ => drones ! Drones.Message.Create)
					complete(StatusCodes.Accepted)
				}
			}
		} ~ path("orders") {
			get {
				complete {
					(ordersMonitor ? OrdersMonitor.Message.GetSnapshot)
						.mapTo[OrdersMonitor.Response.Snapshot]
						.map(_.orders.map { order =>
							OrderApiDto(order.orderId,
								GeolocationApiDto(order.where),
								order.droneId)
						})
				}
			} ~ post {
				entity(as[CreateOrderApiDto]) { order =>
					val where = Geolocation(order.loc.lat, order.loc.long)
					orders ! Orders.Message.CreateOrder(where)
					complete(StatusCodes.Accepted)
				}
			}
		}
	}

	private val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)

	def stop(): Future[akka.Done] = {
		bindingFuture.flatMap(_.unbind())
	}
}