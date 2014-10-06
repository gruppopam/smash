package it.gruppopam.analytics.smash.routing

import akka.actor.{Props, Actor}
import spray.routing.{Route, HttpService}
import it.gruppopam.analytics.smash.core.{Facts, RestMessage, FactsCollector}
import spray.caching.Cache
import it.gruppopam.analytics.smash.FactsJsonSupport._
import com.redis.RedisClient


class RestRouting(implicit val cache: Cache[Array[Byte]],
                  implicit val cachingEnabled: Boolean,
                  implicit val client: RedisClient) extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory = context

  def receive = runRoute(route)

  val route = {
    post {
      path("smash.json") {
        entity(as[Facts]) {
          facts =>
            requestHandler {
              facts
            }

        }
      }
    } ~
      delete {
        path("acceptance.it.gruppopam.analytics.smash.it.gruppopam.analytics.smash.cache") {
          complete {
            cache.clear()
            "OK"
          }
        }
      }
  }

  def requestHandler(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new FactsCollector), message)

}
