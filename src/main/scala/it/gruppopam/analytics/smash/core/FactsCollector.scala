package it.gruppopam.analytics.smash.core

import akka.actor._
import akka.actor.SupervisorStrategy.Escalate

import scala.collection.mutable
import akka.event.Logging
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.OneForOneStrategy
import spray.caching.Cache


class FactsCollector(implicit val cache: Cache[String], implicit val cachingEnabled: Boolean) extends Actor with FactPoster {
  implicit val system: ActorSystem = context.system

  val log = Logging(context.system, this)
  val accumulator = mutable.MutableList[String]()

  def receive = {
    case Facts(urls, params) => {
      import system.dispatcher
      log.info("Started Process!")
      val r = urls.foldRight(List[Future[String]]())((url, acc) => cachedPost(url, params) :: acc)
      log.info("All Requests made!")
      val responses = Future.sequence(r)
      responses onComplete {
        case r => {
          respondToCaller(r.getOrElse(List[String]()))
        }
      }
    }
  }

  def respondToCaller(response: Seq[String]) {
    log.info("Completed Process!")
    context.parent ! CollectedFacts(response).toString
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}