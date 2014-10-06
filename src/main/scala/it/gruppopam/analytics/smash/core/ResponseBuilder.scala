package it.gruppopam.analytics.smash.core

import com.redis.RedisClient
import akka.util.Timeout
import akka.actor.ActorSystem
import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Failure, Try, Success}

case class ResponseBuilder(responses: Seq[Array[Byte]])(implicit val client: RedisClient,
                                                        implicit val timeout: Timeout,
                                                        implicit val system: ActorSystem,
                                                        implicit val executionContext: ExecutionContext) {
  val key = (md5Generator.digest(responses.flatten.toArray) map ("%02x" format _)).mkString

  private def containsKey = client exists key

  private def redisResult: Future[Long] = {
    val p = Promise[Long]()
    containsKey onComplete {
      case Success(x: Boolean) => {
        if (!x) p.completeWith(client.lpush(key, responses)) else p.completeWith(Future[Long](0l))
      }
      case Failure(_) => p.failure(new RuntimeException("Exception when persisting to redis"))
    }
    p.future
  }


  def response = {
    val promise = Promise[Response]()
    redisResult.onComplete {
      case Success(x) => promise.complete(Try(Response(key)))
      case Failure(_) => promise.failure(new RuntimeException("Couldnt write response to redis"))
    }
    promise.future
  }
}