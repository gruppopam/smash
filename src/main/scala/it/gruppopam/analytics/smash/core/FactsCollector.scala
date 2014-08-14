package it.gruppopam.analytics.smash.core

import akka.actor.{Props, ActorRef, Actor, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate

import it.gruppopam.analytics.smash.clients.FactClient.FactResponse
import scala.collection.mutable
import it.gruppopam.analytics.smash.{CollectedFacts, Fact, Facts}
import akka.event.Logging


class FactsCollector(factClient: ActorRef) extends Actor {

  val log = Logging(context.system, this)
  val accumulator = mutable.MutableList[String]()

  def receive = {
    case Facts(urls, params) => {
      for (url <- urls) {
        factClient ! Fact(url, params)
      }
      context become(monitor(urls size))
    }
  }

  def monitor(numMessages: Int) = {
    def respond: Receive = {
      case FactResponse(body) => {
        accumulator += body
        notifySender(numMessages)
      }
    }
    respond
  }

  def notifySender(numMessages: Int) = {
    if (accumulator.size == numMessages)
      context.parent ! CollectedFacts(accumulator).toString
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}