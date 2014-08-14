package it.gruppopam.analytics.smash

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait RestMessage

case class Facts(urls: Seq[String], params: Map[String, String]) extends RestMessage

object FactsJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PortofolioFormats = jsonFormat2(Facts)
}

case class Fact(endpoint: String, params: Map[String, String])


case class CollectedFacts(responses: Seq[String]) {
  override def toString: String = {
    if(responses.size == 0) "[]" else s"[${responses.reduceLeft((a, b) => a + ',' + b)}]"
  }
}

case class Error(message: String)

case class Validation(message: String)