import sbt._

object Dependencies {
	private val akkaVersion = "2.5.11"

	lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

	lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion

	private val akkaHttpVersion = "10.1.0"

	lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

	lazy val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
}
