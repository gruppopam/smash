package it.gruppopam.analytics

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import it.gruppopam.analytics.smash.core.Facts

import akka.util.Timeout
import scala.concurrent.duration._
import com.spray_cache.redis.Formats

package object smash {
  implicit val timeout = Timeout(5 second)
  implicit val format = Formats.byteArrayFormat

  object FactsJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val PortofolioFormats = jsonFormat2(Facts)

  }

}
