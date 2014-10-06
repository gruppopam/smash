package it.gruppopam.analytics.smash.core

import spray.http.HttpData
import com.redis.RedisClient
import akka.util.Timeout
import akka.actor.ActorSystem
import scala.concurrent.{Promise, Future, ExecutionContext}
import scala.util.{Failure, Try, Success}

case class ResponseBuilder(responses: Seq[HttpData])(implicit val client: RedisClient,
                                                     implicit val timeout: Timeout,
                                                     implicit val system: ActorSystem,
                                                     implicit val executionContext: ExecutionContext) {
  val asBytes: Iterable[Array[Byte]] = responses.map(x => x.toByteArray)
  val key = (md5Generator.digest(asBytes.flatten.toArray) map ("%02x" format _)).mkString
  private val redisResult: Future[Long] = client lpush(key, asBytes.asInstanceOf[Seq[Array[Byte]]])


  def response = {
    val promise = Promise[Response]()
    redisResult.onComplete {
      case Success(x) => promise.complete(Try(Response(key)))
      case Failure(_) => promise.failure(new RuntimeException("Couldnt write response to redis"))
    }
    promise.future
  }
}