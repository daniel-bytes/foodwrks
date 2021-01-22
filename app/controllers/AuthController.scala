package controllers

import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticatorService
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.softwaremill.quicklens._
import domain.models._
import domain.services.UsersService
import models.UserPreferencesView
import play.api.Logger
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class AuthController @Inject()(
  val controllerComponents: ControllerComponents,
  usersService: UsersService,
  googleProvider: GoogleProvider,
  authenticatorService: SessionAuthenticatorService
)(implicit ec: ExecutionContext) extends BaseController {
  implicit val logger: Logger = Logger(getClass)

  def login() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login(googleProvider))
  }

  def logout() = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.AuthController.login()).withNewSession
  }

  def authenticateGoogle() = Action.async { implicit request: Request[AnyContent] =>
    googleProvider.authenticate().flatMap {
      case Left(value) => Future.successful(value)

      case Right(authInfo) => for {
        profile <- googleProvider.retrieveProfile(authInfo)

        authUser = googleUser(profile)

        existingUser <- usersService.getUserByExternalId(authUser.externalId).flatMap {
          case Right(user) => Future.successful(Some(user))
          case Left(domain.Error.NotFoundError(_)) => Future.successful(None)
          case Left(e: domain.Error) => Future.failed(e.toDomainException)
        }

        user = existingUser.map(
          _.modify(_.name).setTo(authUser.name).modify(_.email).setTo(authUser.email)
        ).getOrElse(authUser)

        _ <- usersService.saveUser(user).toFuture

        authenticator <- authenticatorService.create(profile.loginInfo)

        value <- authenticatorService.init(authenticator)

        result <- authenticatorService.embed(
          value,
          user.redirectFromPreferences
        )
      } yield result
    }
  }

  private def googleUser(profile: googleProvider.Profile): User =
    User(
      id = UserId.empty,
      externalId = ExternalUserId(profile.loginInfo.providerID, profile.loginInfo.providerKey),
      accountId = AccountId.empty,
      name = (profile.firstName, profile.lastName) match {
        case (Some(first), Some(last)) =>s"$first $last"
        case (Some(first), None) => first
        case (None, Some(last)) => last
        case _ => ""
      },
      email = profile.email.map(UserEmail(_)).getOrElse(UserEmail.unknown),
      preferences = UserPreferencesView.default.toDomainModel
    )
}
