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
	.aggregate(engine, api, main)

lazy val engine = (project in file("engine"))
	.settings(
		commonSettings,
		name := "engine",
		libraryDependencies ++= Seq(
			Dependencies.akkaActor,
			Dependencies.akkaStream
		)
	)

lazy val api = (project in file("api"))
	.settings(
		commonSettings,
		name := "api",
		libraryDependencies ++= Seq(
			Dependencies.akkaHttp,
			Dependencies.sprayJson
		)
	)
	.dependsOn(engine)

lazy val main = (project in file("main"))
	.settings(
		commonSettings,
		name := "main",
		libraryDependencies ++= Seq(
			Dependencies.akkaSlf4j,
			Dependencies.logback
		)
	)
	.dependsOn(api)