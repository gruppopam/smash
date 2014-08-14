package it.gruppopam.analytics.smash.routing

import akka.actor.{ActorRef, Props, Actor}
import spray.routing.{Route, HttpService}
import it.gruppopam.analytics.smash.core.FactsCollector
import it.gruppopam.analytics.smash.{Facts, RestMessage}
import it.gruppopam.analytics.smash.clients.FactClient
import it.gruppopam.analytics.smash.routing.PerRequestCreator
import it.gruppopam.analytics.smash.clients.FactClient

class RestRouting extends HttpService with Actor with PerRequestCreator{

  implicit def actorRefFactory = context

  def receive = runRoute(route)

  val factClient: ActorRef = context.actorOf(Props[FactClient])


  val route = {
    get {
      path("facts") {
        parameters('names) {
          names =>
            requestHandler {
              Facts(
                Seq("http://localhost:201301/custom", "http://localhost:201302/custom"),
                Map("sub_category_id" -> "8001")
              )
            }
        }
      }
    }
  }

  def requestHandler(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new FactsCollector(factClient)), message)
}
