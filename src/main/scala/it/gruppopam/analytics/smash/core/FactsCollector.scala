package it.gruppopam.analytics.smash.core

import akka.actor._
import akka.actor.SupervisorStrategy.Escalate

import akka.event.Logging
import scala.concurrent.{ExecutionContext, Future}
import akka.actor.OneForOneStrategy
import spray.caching.Cache
import scala.util.{Failure, Success}
import com.redis.RedisClient


class FactsCollector(implicit val cache: Cache[Array[Byte]],
                     implicit val cachingEnabled: Boolean,
                     implicit val client: RedisClient) extends Actor with FactPoster {
  implicit val system: ActorSystem = context.system

  val log = Logging(context.system, this)

  def receive = {
    case Facts(facts) => {
      import system.dispatcher
      log.info("Started Process!")
      val r = facts.foldRight(List[Future[Array[Byte]]]())((fact, acc) => cachedPost(fact) :: acc)
      log.info("All Requests made!")
      val responses = Future.sequence(r)
      responses onComplete {
        case Success(r: Seq[Array[Byte]]) => respondToCaller(r)
        case Failure(f) =>
          throw new RuntimeException(f)
      }
    }
  }

  def respondToCaller(response: Seq[Array[Byte]])(implicit ec: ExecutionContext) {
    log.info("Completed Process!")
    ResponseBuilder(response).response onComplete {
      case Success(x: Response) => context.parent ! x
      case Failure(x) => {
        throw new RuntimeException(x)
      }
    }
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}