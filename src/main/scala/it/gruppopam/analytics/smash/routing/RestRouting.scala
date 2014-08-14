package it.gruppopam.analytics.smash.routing

import akka.actor.{ActorRef, Props, Actor}
import spray.routing.{Route, HttpService}
import it.gruppopam.analytics.smash.core.FactsCollector
import it.gruppopam.analytics.smash.{Facts, RestMessage}
import it.gruppopam.analytics.smash.clients.FactClient
import it.gruppopam.analytics.smash.FactsJsonSupport._

class RestRouting extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory = context

  def receive = runRoute(route)

  val factClient: ActorRef = context.actorOf(Props[FactClient])

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
    }
  }

  def requestHandler(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new FactsCollector(factClient)), message)
}
