package it.gruppopam.analytics.smash.cache

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.concurrent.ScalaFutures._

class RedisBackedMapUnit extends FlatSpec with Matchers {

  "remove" should "remove associated values in underlying store" in {
    val store = RedisBackedMap[String](10, 10)
    store.putIfAbsent("key1", Future("This is a test!"))
    whenReady(store.remove("key1")) {
      value =>
        value shouldBe "This is a test!"
    }
  }

  "maxCapacity" should "be greater greater than 0" in {
    a[IllegalArgumentException] should be thrownBy {
      RedisBackedMap[String](-1, -1)
    }
  }

  "initialCapacity" should "be <= maxCapacity" in {
    a[IllegalArgumentException] should be thrownBy {
      RedisBackedMap[String](1, 10)
    }
  }

  "putIfAbsent" should "put if absent" in {
    val store = RedisBackedMap[String](10, 10)
    store.size shouldBe 0
    store.putIfAbsent("key1", Future("This is a test!"))
    store.size shouldBe 1
    store.putIfAbsent("key1", Future("This is a test!"))
    store.size shouldBe 1
  }

  "get" should "get from the underlying store" in {
    val store = RedisBackedMap[String](10, 10)
    store.get("key1") shouldBe null
    store.putIfAbsent("key1", Future("This is a test!"))
    whenReady(store.get("key1")) {
      value =>
        value shouldBe "This is a test!"
    }
  }

  "clear" should "purge all entries from the underlying store" in {
    val store = RedisBackedMap[String](10, 10)
    store.putIfAbsent("key1", Future("This is a test!"))
    store.size shouldBe 1
    store.clear()

    store.size shouldBe 0
    store.get("key1") shouldBe null
  }
}
