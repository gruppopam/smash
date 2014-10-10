package it.gruppopam.analytics.smash.core


import akka.util.Timeout
import akka.actor.ActorSystem
import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Failure, Try, Success}
import scala.concurrent.duration._
import redis.RedisClient

case class ResponseBuilder(responses: Seq[Array[Byte]])(implicit val system: ActorSystem,
                                                        implicit val executionContext: ExecutionContext,
                                                        implicit val client: RedisClient) {
  implicit val timeout = Timeout(10 seconds)

  system.log.info("Writing #bytes:" + responses.flatten.toArray.length)
  private[ResponseBuilder] val key = s"${random.nextLong().toString}"
  private[ResponseBuilder] val pushToRedis: Future[Long] = client.lpush(key, responses: _*)

  private val enrichedWithTimeout = {
    val p = Promise[Boolean]()
    pushToRedis onComplete {
      case Success(_) => p completeWith (client expire(key, 25))
      case Failure(x) =>
        x.printStackTrace()
        p.failure(new RuntimeException("Exception when persisting to redis"))
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