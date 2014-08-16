package it.gruppopam.analytics.smash

import akka.actor.{Props, ActorSystem}

import it.gruppopam.analytics.smash.routing.RestRouting

import spray.can.Http
import akka.io.IO
import spray.caching.{SimpleLruCache, Cache}
import org.greencheek.spray.cache.memcached.MemcachedCache

object Boot extends App {
  implicit val system = ActorSystem("analytics-smash")

  implicit val cache: Cache[String] =  new MemcachedCache[String](maxCapacity = 50000)

  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")

  system.registerOnTermination {
    system.log.info("Oh no.. I was expecting more smashing. Bring me up soon!")
  }

  IO(Http) ! Http.Bind(serviceActor, "localhost", port = 38080)
}
