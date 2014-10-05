package it.gruppopam.analytics.smash

import akka.actor.{Props, ActorSystem}

import it.gruppopam.analytics.smash.routing.RestRouting

import spray.can.Http
import akka.io.IO
import spray.caching.Cache

object Boot extends App {
  implicit val system = ActorSystem("analytics-smash")

  implicit val cachingEnabled = sys.props.getOrElse("enableCaching", "false").toBoolean

  implicit val cache: Cache[String] = null
  //  implicit val acceptance.it.gruppopam.analytics.smash.it.gruppopam.analytics.smash.cache: Cache[String] = if(cachingEnabled) new MemcachedCache[String](maxCapacity = 50000, allowFlush = true) else null

  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")

  system.registerOnTermination {
    system.log.info("Oh no.. I was expecting more smashing. Bring me up soon!")
  }

  IO(Http) ! Http.Bind(serviceActor, "localhost", port = 38080)
}
