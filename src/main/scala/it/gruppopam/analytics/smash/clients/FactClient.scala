package it.gruppopam.analytics.smash.clients

import akka.actor.Actor
import it.gruppopam.analytics.smash.Fact
import it.gruppopam.analytics.smash.clients.FactClient.FactResponse

class FactClient extends Actor {
  def receive = {
    case Fact(url, params) => {
      sender ! FactResponse(
        """
          {"a": 1 }
        """)
    }
  }
}

object FactClient {
  case class FactResponse(body: String)
}

