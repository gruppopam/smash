package it.gruppopam.analytics.smash.core

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.http._
import spray.can.Http
import HttpMethods._
import spray.caching.Cache

trait FactPoster {

  implicit val timeout: Timeout = 5.seconds

  implicit def system: ActorSystem

  implicit def cache: Cache[Array[Byte]]

  implicit def cachingEnabled: Boolean


  def mapToParams(params: Map[String, String]): String = {
    params.foldLeft(List[String]()) {
      case (a, (k, v)) => a :+ s"$k=$v"
    }.mkString("&")
  }

  def cachedPost(fact: Fact)(implicit ec: ExecutionContext): Future[Array[Byte]] = withCaching(fact)

  private def post(url: String, params: Map[String, String])(implicit ec: ExecutionContext): Future[Array[Byte]] = {
    system.log.debug(s"Performing request -> $url -> $params")
    for {
      response <- IO(Http).ask(HttpRequest(POST, url)
        .withEntity(HttpEntity(ContentTypes.`text/plain`, mapToParams(params))))
        .mapTo[HttpResponse]
    } yield {
      response.entity.data.toByteArray
    }
  }

  private def withCaching(fact: Fact)(implicit ec: ExecutionContext) = {
    if (cachingEnabled) cache(keyGen(fact.url, fact.params), () => post(fact.url, fact.params)) else post(fact.url, fact.params)
  }

  private def keyGen(url: String, params: Map[String, String]) = {
    val key = s"""$url-${params.toList.sortBy(_._1).toMap.toString()}"""
    (md5Generator digest key.getBytes map ("%02x" format _)).mkString
  }
}

