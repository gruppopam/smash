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
  private[ResponseBuilder] val key = random.nextLong().toString

  private[ResponseBuilder] val pushToRedis: Future[Long] = client.lpush(key, responses)

  private val enrichedWithTimeout = {
    val p = Promise[Boolean]()
    pushToRedis onComplete {
      case Success(_) => p completeWith (client expire(key, 25))
      case Failure(_) => p.failure(new RuntimeException("Exception when persisting to redis"))
    }
    p.future
  }


  def response = {
    val promise = Promise[Response]()
    enrichedWithTimeout onComplete {
      case Success(x) => promise.complete(Try(Response(key)))
      case Failure(_) => promise.failure(new RuntimeException("Couldn't write response to redis"))
    }
    promise.future
  }
}