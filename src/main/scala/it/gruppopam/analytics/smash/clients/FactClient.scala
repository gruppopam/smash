package it.gruppopam.analytics.smash.clients

import akka.actor.Actor
import it.gruppopam.analytics.smash.Fact
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.http._
import spray.can.Http
import HttpMethods._
import scala.util.Success
import scala.util.Failure
import it.gruppopam.analytics.smash.Error
import akka.event.{LoggingAdapter, Logging}

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
      system.log.debug("Received Response:" +  response.status + response.entity.asString)
      response.entity.asString
    }
  }
}

class FactClient extends Actor with FactPoster {
  implicit val system: ActorSystem = context.system
  import system.dispatcher

  def receive = {
    case Fact(url, params) => {
      val response = for {
        response <- post(url, params)
      } yield (response)
      val origSender = sender
      response onComplete {
        case Success(body) => {
          origSender ! FactResponse(body)
        }
        case Failure(error) => {
          origSender ! Error(error.getMessage)
        }
      }
    }
  }
}

case class FactResponse(body: String)


