package io.scalac.akka.demo.api.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

private[api] case class DroneApiDto(id: String, loc: GeolocationApiDto, age: Int)

private[api] object DroneApiDto extends SprayJsonSupport with DefaultJsonProtocol {
	implicit val format: RootJsonFormat[DroneApiDto] = jsonFormat3(DroneApiDto.apply)
}