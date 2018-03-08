lazy val commonSettings = Seq(
	organization := "io.scalac",
	version := "0.1.0-SNAPSHOT",
	scalaVersion := "2.12.4"
)

lazy val root = (project in file("."))
	.settings(
		commonSettings,
		name := "akka-demo",
	)

lazy val engine = (project in file("engine"))
	.settings(
		commonSettings,
		name := "engine",
		libraryDependencies += Dependencies.akkaActor
	)
