package it.gruppopam.analytics.smash.cache

import scala.concurrent.Future
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap

case class RedisBackedMap[V](maxCapacity: Int, initialCapacity: Int) {
  require(maxCapacity > 0, "maxCapacity must be greater than 0")
  require(initialCapacity <= maxCapacity, "initialCapacity must be <= maxCapacity")

  private[RedisBackedMap] val store = new ConcurrentLinkedHashMap.Builder[String, Future[V]]
    .initialCapacity(initialCapacity)
    .maximumWeightedCapacity(maxCapacity)
    .build()

  def putIfAbsent(key: String, value: Future[V]) = {
    store.putIfAbsent(key, value)
  }

  def remove(key: String) = store.remove(key)

  def get(key: String) = store.get(key)

  def size: Int = store.size

  def clear() = store.clear()
}