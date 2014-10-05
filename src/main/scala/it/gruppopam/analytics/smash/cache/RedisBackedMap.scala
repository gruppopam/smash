package it.gruppopam.analytics.smash.cache

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import scala.util.{Failure, Success}
import com.redis.RedisClient
import akka.util.Timeout
import akka.actor.ActorSystem
import scala.concurrent._

case class RedisBackedMap(maxCapacity: Int, initialCapacity: Int)
                         (implicit val client: RedisClient,
                          implicit val timeout: Timeout,
                          implicit val system: ActorSystem,
                          implicit val executionContext: ExecutionContext) {

  require(maxCapacity > 0, "maxCapacity must be greater than 0")
  require(initialCapacity <= maxCapacity, "initialCapacity must be <= maxCapacity")

  private[RedisBackedMap] val store = new ConcurrentLinkedHashMap.Builder[String, Future[Array[Byte]]]
    .initialCapacity(initialCapacity)
    .maximumWeightedCapacity(maxCapacity)
    .build()

  def putIfAbsent(key: String, genValue: () => Future[Array[Byte]]): Future[Array[Byte]] = {
    val promise = Promise[Array[Byte]]()
    store.putIfAbsent(key.toString, promise.future) match {
      case null ⇒
        val future = genValue()
        future.onComplete {
          case value@Success(_) ⇒
            val write: Future[Boolean] = redisWrite(key, value get)
            write onComplete {
              case Success(_) => promise.complete(value)
              case Failure(_) => promise.failure(new RuntimeException("Error when persisting to Redis"))
            }
          case value@Failure(_) ⇒
            store.remove(key.toString)
            promise.complete(value)
        }
        promise.future
      case existingFuture ⇒ existingFuture
    }

  }

  def remove(key: String): Future[Array[Byte]] = {
    client.del(key)
    store.remove(key)
  }

  def get(key: String): Future[Array[Byte]] = {
    if (store.containsKey(key)) return store.get(key)
    store.remove(key)

    val fromRedis: Future[Array[Byte]] = redisGet(key)
    fromRedis onComplete {
      case Success(x) => store.put(key, fromRedis)
      case Failure(x) => {
        println(x)
        throw new RuntimeException("Failure when talking to redis")
      }
    }
    fromRedis
  }

  def size = store.size()

  def clear() = {
    client.flushall()
    store.clear()
  }

  def redisWrite(key: String, value: Array[Byte]) = {
    client.set(key, value)
  }

  def redisGet(key: String) = for {
    res <- client.get[Array[Byte]](key)
  } yield {
    res.get
  }
}