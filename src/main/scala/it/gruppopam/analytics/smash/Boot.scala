package it.gruppopam.analytics.smash

import akka.actor.{Props, ActorSystem}

import it.gruppopam.analytics.smash.routing.RestRouting

import spray.can.Http
import akka.io.IO
import spray.caching.Cache
import com.redis.RedisClient
import it.gruppopam.analytics.smash.cache.BinaryRedisCache

object Boot extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("analytics-smash")

  implicit val cachingEnabled = sys.props.getOrElse("enableCaching", "false").toBoolean

  implicit val client = RedisClient("localhost", 6379)

  implicit val cache: Cache[Array[Byte]] = if (cachingEnabled) new BinaryRedisCache(maxCapacity = 50000, initialCapacity = 0) else null

  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")

  system.registerOnTermination {
    system.log.info("Oh no.. I was expecting more smashing. Bring me up soon!")
  }

  IO(Http) ! Http.Bind(serviceActor, "localhost", port = 38080)
}
