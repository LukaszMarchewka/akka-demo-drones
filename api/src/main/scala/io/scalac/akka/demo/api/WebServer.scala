package io.scalac.akka.demo.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import io.scalac.akka.demo.api.model.{CreateDronesApiDto, DroneApiDto, GeolocationApiDto, OrderApiDto}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class WebServer(implicit val system: ActorSystem) extends Directives {
	private implicit val materializer: ActorMaterializer = ActorMaterializer()
	private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
	private implicit val timeout: Timeout = 100.millis

	private val route: Route = pathPrefix("api") {
		path("drones") {
			get {
				//todo implement it
				complete(List(DroneApiDto("1", GeolocationApiDto(1.1, 2.2))))
			} ~ post {
				entity(as[CreateDronesApiDto]) { drone =>
					//todo implement it
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