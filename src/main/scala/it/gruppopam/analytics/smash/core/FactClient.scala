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
import spray.caching.{SimpleLruCache, Cache}

trait FactPoster {
  import ExecutionContext.Implicits.global

  implicit val timeout: Timeout = 5.seconds

  implicit def system: ActorSystem

  implicit def cache: Cache[String]

  def mapToParams(params: Map[String, String]): String = {
    (params.foldLeft(List[String]()) {
      case (a, (k, v)) => a :+ s"${k}=${v}"
    }).mkString("&")
  }


  def cachedPost(url:String, params:Map[String, String]): Future[String] = cache(cacheKeyFromUrlsAndParams(url, params)) {
    post(url, params)
  }

  def post(url: String, params: Map[String, String]): Future[String] = {
    system.log.info("Performing request ->")
    for {
      response <- IO(Http).ask(HttpRequest(POST, url)
        .withEntity(HttpEntity(ContentTypes.`text/plain`, mapToParams(params))))
        .mapTo[HttpResponse]
    } yield {
      response.entity.asString
    }
  }

  private def cacheKeyFromUrlsAndParams(url: String, params: Map[String, String]) = {
    s"${url}-${params.toList.sortBy(_._1).toMap.toString}"
  }
}

