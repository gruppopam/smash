package it.gruppopam.analytics

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import it.gruppopam.analytics.smash.core.Facts

package object smash {

  object FactsJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
    implicit val PortofolioFormats = jsonFormat2(Facts)
  }

}
