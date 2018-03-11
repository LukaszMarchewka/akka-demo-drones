package io.scalac.akka.demo.api.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

private[api] case class CreateDronesApiDto(number: Int)

private[api] object CreateDronesApiDto extends SprayJsonSupport with DefaultJsonProtocol {
	implicit val format: RootJsonFormat[CreateDronesApiDto] = jsonFormat1(CreateDronesApiDto.apply)
}