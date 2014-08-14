package it.gruppopam.analytics.smash

import akka.actor.{Props, ActorSystem}
import it.gruppopam.analytics.smash.routing.RestRouting
import akka.io.IO
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem("analytics-smash")

  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")

  system.registerOnTermination {
    system.log.info("Oh no.. I was expecting more smashing. Bring me up soon!")
  }

  IO(Http) ! Http.Bind(serviceActor, "localhost", port = 38080)
}
