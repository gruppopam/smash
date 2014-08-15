package it.gruppopam.analytics.smash.clients

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.http._
import spray.can.Http
import HttpMethods._

trait FactPoster {
  implicit val timeout: Timeout = 5.seconds

  def mapToParams(params: Map[String, String]): String = {
    (params.foldLeft(List[String]()) {
      case (a, (k, v)) => a :+ s"${k}=${v}"
    }).mkString("&")
  }

  def post(url: String, params: Map[String, String])(implicit system: ActorSystem): Future[String] = {
    import system.dispatcher
    for {
      response <- IO(Http).ask(HttpRequest(POST, url)
        .withEntity(HttpEntity(ContentTypes.`text/plain`, mapToParams(params))))
        .mapTo[HttpResponse]
    } yield {
      response.entity.asString
    }
  }
}

