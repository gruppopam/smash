package it.gruppopam.analytics.smash.core

import akka.actor._
import akka.actor.SupervisorStrategy.Escalate

import akka.event.Logging
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.OneForOneStrategy
import spray.caching.Cache
import spray.http.HttpData
import scala.util.Success
import com.redis.RedisClient


class FactsCollector(implicit val cache: Cache[HttpData],
                     implicit val cachingEnabled: Boolean,
                      implicit val client: RedisClient) extends Actor with FactPoster {
  implicit val system: ActorSystem = context.system

  val log = Logging(context.system, this)

  def receive = {
    case Facts(urls, params) => {
      import system.dispatcher
      log.info("Started Process!")
      val r = urls.foldRight(List[Future[HttpData]]())((url, acc) => cachedPost(url, params) :: acc)
      log.info("All Requests made!")
      val responses = Future.sequence(r)
      responses onComplete {
        case r => {
          respondToCaller(r.getOrElse(List[HttpData]()))
        }
      }
    }
  }

  def respondToCaller(response: Seq[HttpData])(implicit ec: ExecutionContext) {
    log.info("Completed Process!")
    ResponseBuilder(response).response onComplete {
      case Success(x: Response) => context.parent ! x
    }
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}