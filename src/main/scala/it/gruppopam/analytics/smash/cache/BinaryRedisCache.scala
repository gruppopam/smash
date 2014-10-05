package it.gruppopam.analytics.smash.cache

import spray.caching.Cache
import scala.concurrent.{ExecutionContext, Future}
import it.gruppopam.analytics.smash.cache.RedisSystem._


object BinaryRedisCache {
  def apply(maxCapacity: Int = 500,
            initialCapacity: Int = 16) = {

  }
}

class BinaryRedisCache(maxCapacity: Int, initialCapacity: Int)(implicit ec: ExecutionContext) extends Cache[Array[Byte]] {

  private[BinaryRedisCache] val store = RedisBackedMap(maxCapacity, initialCapacity)

  override def size: Int = store.size

  override def clear(): Unit = store.clear

  override def remove(key: Any): Option[Future[Array[Byte]]] = Option(store.remove(key.toString))

  override def get(key: Any): Option[Future[Array[Byte]]] = Option(store.get(key.toString))

  override def apply(key: Any, genValue: () => Future[Array[Byte]])(implicit ec: ExecutionContext): Future[Array[Byte]] = {
    store.putIfAbsent(key.asInstanceOf[String], genValue)
  }
}
