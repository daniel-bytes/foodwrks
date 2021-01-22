package controllers

import com.mohiva.play.silhouette.api.Silhouette
import play.api.Logger

import javax.inject._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  val controllerComponents: ControllerComponents,
  silhouette: Silhouette[Auth.SessionEnv]
) extends BaseController {
  implicit val logger: Logger = Logger(getClass)

  def index() = silhouette.SecuredAction { implicit request =>
    request.identity.user.redirectFromPreferences
  }
}
