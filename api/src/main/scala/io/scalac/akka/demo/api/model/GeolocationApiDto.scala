package io.scalac.akka.demo.api.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.scalac.akka.demo.types.Geolocation
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

private[api] case class GeolocationApiDto(lat: Double, long: Double)

private[api] object GeolocationApiDto extends SprayJsonSupport with DefaultJsonProtocol {
	implicit val format: RootJsonFormat[GeolocationApiDto] = jsonFormat2(GeolocationApiDto.apply)

	def apply(loc: Geolocation): GeolocationApiDto = GeolocationApiDto(loc.lat, loc.long)
}