package it.gruppopam.analytics

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import it.gruppopam.analytics.smash.core.{Fact, Facts}

import akka.util.Timeout
import scala.concurrent.duration._
import com.spray_cache.redis.Formats

package object smash {
  implicit val timeout = Timeout(10 seconds)
  implicit val format = Formats.format

  object FactsJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val endpointFormat = jsonFormat2(Fact)
    implicit val endpointWrapperFormat = jsonFormat1(Facts)
  }

}
