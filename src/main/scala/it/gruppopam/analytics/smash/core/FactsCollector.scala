package it.gruppopam.analytics.smash.core

import akka.actor._
import akka.actor.SupervisorStrategy.Escalate

import scala.collection.mutable
import akka.event.Logging
import it.gruppopam.analytics.smash.clients.FactPoster
import scala.concurrent.{ExecutionContext, Future}
import it.gruppopam.analytics.smash.CollectedFacts
import it.gruppopam.analytics.smash.Facts
import akka.actor.OneForOneStrategy


class FactsCollector extends Actor with FactPoster {
  implicit val system: ActorSystem = context.system

  val log = Logging(context.system, this)
  val accumulator = mutable.MutableList[String]()

  def receive = {
    case Facts(urls, params) => {
      import ExecutionContext.Implicits.global
      log.info("Started Process")
      val r = urls.foldRight(List[Future[String]]())((url, acc) => post(url, params) :: acc)
      val responses = Future.sequence(r)
      responses onComplete {
        case r => {
          respondToCaller(r.getOrElse(List[String]()))
        }
      }
    }
  }

  def respondToCaller(response: Seq[String]) {
    log.info("Completed Process")
    context.parent ! CollectedFacts(response).toString
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}