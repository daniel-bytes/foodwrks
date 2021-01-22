name := """foodwrks"""
organization := "dev.danielbytes"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.4"

// Needed for play-silhouette
// see https://stackoverflow.com/questions/58941734/sbt-librarymanagement-resolveexception-error-downloading-com-atlassian-jwtjwt-c
resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
libraryDependencies += "com.softwaremill.quicklens" %% "quicklens" % "1.6.1"
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.11"
libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "7.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "7.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "7.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "7.0.0",
  "com.mohiva" %% "play-silhouette-testkit" % "7.0.0" % "test"
)
libraryDependencies += jdbc % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.10" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.7.19" % Test
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "dev.danielbytes.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "dev.danielbytes.binders._"
