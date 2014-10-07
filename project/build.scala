import sbt._

object SmashBuild extends Build {

  lazy val root = Project("root", file("."))
    .dependsOn(sprayRedis)

  lazy val sprayRedis = RootProject(uri("git://github.com/jpsimonroy/spray-redis.git"))

}