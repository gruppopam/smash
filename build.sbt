
assemblySettings

net.virtualvoid.sbt.graph.Plugin.graphSettings

organization in ThisBuild := "com.thoughtworks"

name := """smash"""

version := "1.0"

scalaVersion := "2.10.0"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8", "-language:postfixOps")

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.4",
  "io.spray" % "spray-can" % "1.2.1",
  "io.spray" % "spray-routing" % "1.2.1",
  "io.spray" % "spray-caching" % "1.2.1",
  "io.spray" %% "spray-json" % "1.2.6",
  "org.greencheek.spray" % "spray-cache-spymemcached" % "0.2.2"
)