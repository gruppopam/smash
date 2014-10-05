
assemblySettings

net.virtualvoid.sbt.graph.Plugin.graphSettings

organization in ThisBuild := "com.thoughtworks"

name := """smash"""

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8", "-language:postfixOps", "-Yrangepos")

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.specs2" %% "specs2" % "2.4.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "io.spray" %% "spray-can" % "1.3.1",
  "io.spray" %% "spray-routing" % "1.3.1",
  "io.spray" %% "spray-caching" % "1.3.1",
  "io.spray" %% "spray-json" % "1.3.0",
  "net.debasishg" %% "redisreact" % "0.6"
)