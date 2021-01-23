package controllers

import com.mohiva.play.silhouette.api.Silhouette
import play.api.Logger

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  controllerComponents: MessagesControllerComponents,
  silhouette: Silhouette[Auth.SessionEnv]
)(implicit ec: ExecutionContext) extends MessagesAbstractController(controllerComponents) {
  implicit val logger: Logger = Logger(getClass)

  def index() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      Future.successful(user.redirectFromPreferences())
    }
  }
}
