package acceptance.it.gruppopam.analytics.smash.cache


import org.specs2.Specification
import org.specs2.specification.Fragments
import it.gruppopam.analytics.smash.cache.RedisBackedMap

class RedisBackedMapSpec extends Specification {
  override def is: Fragments = s2"""
    ${"Creation and Operations on RedisBackedMap".title}
        Operations on the store can be          $endp
          1. Initialization                     $initialization
                                                                  """

  def initialization = s2"""
   Initialization can be                                $endp
    1. Successful                                       ${create(10, 10).size mustEqual 0}
    2. Fail when max capacity is 0                      ${create(10, 0) must throwAn[IllegalArgumentException]}
                                                                                """


  def create(initialCapacity: Int, maxCapacity: Int) = {
    RedisBackedMap(maxCapacity, initialCapacity)
  }
}