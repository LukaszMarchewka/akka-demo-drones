package io.scalac.akka.demo.api.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

private[api] case class CreateOrderApiDto(loc: GeolocationApiDto)

private[api] object CreateOrderApiDto extends SprayJsonSupport with DefaultJsonProtocol {
	implicit val format: RootJsonFormat[CreateOrderApiDto] = jsonFormat1(CreateOrderApiDto.apply)
}