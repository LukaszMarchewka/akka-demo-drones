package io.scalac.akka.demo.types

import io.scalac.akka.demo.types.Geolocation.{Latitude, Longitude}

case class Geolocation(lat: Latitude, long: Longitude)

object Geolocation {
	type Latitude = Double
	type Longitude = Double

	/**
	  * Calculates a distance between two locations.
	  *
	  * @param loc1 first location.
	  * @param loc2 second location.
	  * @return the distance between them.
	  */
	def distance(loc1: Geolocation, loc2: Geolocation): Double = {
		Math.sqrt(Math.pow(loc1.lat - loc2.lat, 2) + Math.pow(loc1.long - loc2.long, 2))
	}

	/**
	  * Calculates delta between two locations.
	  *
	  * @param from  starting location.
	  * @param to    target location.
	  * @param speed speed of movement.
	  * @return delta.
	  */
	def delta(from: Geolocation, to: Geolocation, speed: Double): Geolocation = {
		val steps = Geolocation.distance(from, to) / speed
		val latDelta = (to.lat - from.lat) / steps
		val longDelta = (to.long - from.long) / steps
		Geolocation(latDelta, longDelta)
	}
}