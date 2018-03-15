package io.scalac.akka.demo.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.scalac.akka.demo.api.model.{CreateDronesApiDto, DroneApiDto, GeolocationApiDto, OrderApiDto}
import io.scalac.akka.demo.drone.Drones
import io.scalac.akka.demo.monitoring.DronesRadar

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class WebServer(drones: ActorRef, dronesRadar: ActorRef)(implicit val system: ActorSystem) extends Directives {
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
							DroneApiDto(drone.droneId, GeolocationApiDto(drone.loc.lat, drone.loc.long))
						})
				}
			} ~ post {
				entity(as[CreateDronesApiDto]) { request =>
					(1 to request.number).foreach(_ => drones ! Drones.Message.Create)
					complete(StatusCodes.Accepted)
				}
			}
		} ~ path("orders") {
			post {
				entity(as[OrderApiDto]) { order =>
					//todo implement it
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