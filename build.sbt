name := """traffic_light_playws"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  guice,
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.h2database" % "h2" % "1.4.199",
  specs2 % Test,
  ws,
  "com.typesafe" % "config" % "1.4.0",
  "org.webjars" % "jquery" % "2.1.3",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.mockito" %% "mockito-scala" % "1.16.0",
  ehcache,
  evolutions
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
