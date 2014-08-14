package it.gruppopam.analytics.smash.routing

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import spray.http.{ContentTypes, HttpEntity, StatusCode}
import it.gruppopam.analytics.smash.routing.PerRequest._
import it.gruppopam.analytics.smash.{Error, Validation, RestMessage}

trait PerRequest extends Actor {

  import context._

  def r: RequestContext
  def target: ActorRef
  def message: RestMessage

  setReceiveTimeout(2.seconds)
  target ! message

  def receive = {
    case res: String => complete(OK, res)
    case ReceiveTimeout   => complete(GatewayTimeout, "Request timeout")
  }

  def complete(status: StatusCode, body: String) = {
    r.complete(status, HttpEntity(ContentTypes.`application/json`, body))
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, e.getMessage)
        Stop
      }
    }
}

object PerRequest {
  case class WithActorRef(r: RequestContext, target: ActorRef, message: RestMessage) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: RestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator {
  this: Actor =>

  def perRequest(r: RequestContext, target: ActorRef, message: RestMessage) =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequest(r: RequestContext, props: Props, message: RestMessage) =
    context.actorOf(Props(new WithProps(r, props, message)))
}