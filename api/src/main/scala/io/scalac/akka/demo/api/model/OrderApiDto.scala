package io.scalac.akka.demo.api.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

private[api] case class OrderApiDto(id: String, loc:GeolocationApiDto)

private[api] object OrderApiDto extends SprayJsonSupport with DefaultJsonProtocol {
	implicit val format: RootJsonFormat[OrderApiDto] = jsonFormat2(OrderApiDto.apply)
}