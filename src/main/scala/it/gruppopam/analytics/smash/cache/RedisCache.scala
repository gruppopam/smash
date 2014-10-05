package it.gruppopam.analytics.smash.cache

import spray.caching.Cache
import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.util.{Failure, Success}


object RedisCache {
  def apply[V](maxCapacity: Int = 500,
               initialCapacity: Int = 16) = {

  }
}

class RedisCache[V](maxCapacity: Int, initialCapacity: Int)(implicit ec: ExecutionContext) extends Cache[V] {

  private[RedisCache] val store = RedisBackedMap[V](maxCapacity, initialCapacity)

  override def size: Int = store.size

  override def clear(): Unit = store.clear

  override def remove(key: Any): Option[Future[V]] = Option(store.remove(key.toString))

  override def get(key: Any): Option[Future[V]] = Option(store.get(key.toString))

  override def apply(key: Any, genValue: () => Future[V])(implicit ec: ExecutionContext): Future[V] = {
    val promise = Promise[V]()
    store.putIfAbsent(key.toString, promise.future) match {
      case null ⇒
        val future = genValue()
        future.onComplete {
          value ⇒
            value match {
              case Success(_) ⇒ value
              case Failure(_) ⇒ store.remove(key.toString)
            }
            promise.complete(value)
        }
        future
      case existingFuture ⇒ existingFuture
    }
  }
}
